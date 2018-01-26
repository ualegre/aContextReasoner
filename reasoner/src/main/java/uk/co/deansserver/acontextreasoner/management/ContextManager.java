/*
 * Copyright 2017 aContextReasoner Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.deansserver.acontextreasoner.management;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.database.ContextDB;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.utility.ClassPackage;
import uk.co.deansserver.acontextreasoner.utility.Prefs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;
import uk.ac.mdx.cs.ie.acontextlib.ContextObserver;
import uk.ac.mdx.cs.ie.acontextlib.ContextReceiver;
import uk.ac.mdx.cs.ie.acontextlib.ContextReceivers;
import uk.ac.mdx.cs.ie.acontextlib.IContextManager;
import uk.ac.mdx.cs.ie.acontextlib.IReasonerManager;

import static android.content.Context.MODE_PRIVATE;

/**
 * Class to handle runtime management of Context observers and receivers.
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ContextManager implements IContextManager {

    private Context mContext;
    private static final String LOGTAG = "ContextManager";
    private final HashMap<String, ContextObserver> mActiveContexts = new HashMap<>();
    private ContextDB mContextDatabase;
    private ContextReceivers mContextReceivers;
    private ExternalContextReceiver mExternalContextReceiver;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar mCalendar = Calendar.getInstance();
    private ContextReasonerCore mReasonerCore;
    private DataLogger mLogger;

    public ContextManager(Context c, ContextReasonerCore reasonerCore, ContextDB db) {

        mContext = c;
        mReasonerCore = reasonerCore;
        mContextDatabase = db;
        mLogger = reasonerCore.getLogger();
        mContextReceivers = new ContextReceivers();
        mContextReceivers.add("core", new CoreReceiver(this, mReasonerCore.getReasonerManager(), mLogger));
        loadAllOtherReceiverClasses();
        mExternalContextReceiver = new ExternalContextReceiver(this, mLogger);
        IntentFilter filter = new IntentFilter(mExternalContextReceiver.CONTEXT_INTENT);
        mContext.registerReceiver(mExternalContextReceiver, filter);
    }

    public boolean addObserverRequirement(String appkey, String observerName) {

        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer != null) {
            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER, LOGTAG,
                    "Observer " + observerName + " already running, adding requirement");
            observer.addRequiringApp(appkey);
            return true;
        } else {
            observer = loadContextClass(appkey, observerName);

            if (observer == null) {
                mLogger.logError(DataLogger.CONTEXT_MANAGER, LOGTAG,
                        "Observer " + observerName + " could not be loaded");
                return false;
            } else {

                if (! observer.start()) {
                    observer.removeAllRequiringApps();
                    mActiveContexts.remove(observerName);
                    observer = null;
                    return false;
                } else {
                    mLogger.logVerbose(DataLogger.CONTEXT_MANAGER, LOGTAG,
                            "Observer " + observerName + " started");
                    return true;
                }
            }
        }
    }

    public boolean startObserver(String appkey, String observerName) {

        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer != null) {
            if (! observer.isARequiringApp(appkey) ) {
                mLogger.logError(DataLogger.CONTEXT_MANAGER,
                        LOGTAG, "Observer " + observerName + " is not needed by app");
                return false;
            } else {
                if (! observer.isRunning()) {
                    mLogger.logError(DataLogger.CONTEXT_MANAGER, LOGTAG,
                            "Observer " + observerName + " is running already");
                    return false;
                } else {
                    if (! observer.start()) {
                        observer.removeAllRequiringApps();
                        mActiveContexts.remove(observerName);
                        return false;
                    } else {
                        return true;
                    }

                }
            }
        } else {
            mLogger.logError(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Observer " + observerName + " needs to be loaded first");
            return false;
        }
    }

    public boolean removeObserverRequirement(String appkey, String observerName) {
        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer != null) {
            observer.removeRequiringApp(appkey);

            if (observer.numberOfRequiringApps()<1) {
                mLogger.logVerbose(DataLogger.CONTEXT_MANAGER,
                        LOGTAG, "Observer " + observerName + " no longer needed, shutting down");
                if (observer.isRunning()) {
                    observer.stop();
                }
                mActiveContexts.remove(observerName);
            }

            return true;
        } else {
            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Observer " + observerName + " not running! Ignoring");
            return false;
        }
    }

    /*public void copyDexFile(String appKey, final String newDex,
                            String[] contexts, String packagename, int permission) {

        File dexInternalStoragePath = new File(mContext.getDir("dex",
                        Context.MODE_PRIVATE), newDex);

        String celoc = Environment.getExternalStorageDirectory()
                        + "/Android/data/org.poseidon_project/files/";
        File newDexFile = new File(celoc, newDex);

        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        final int BUF_SIZE = 8 * 1024;

        try {

            bis = new BufferedInputStream(new FileInputStream(newDexFile));
            dexWriter = new BufferedOutputStream(new FileOutputStream(
                            dexInternalStoragePath));

            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0 ) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            for (String c: contexts) {
                mContextDatabase.insertComponent(packagename, c, appKey, permission, newDex);
            }

            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER,
                    contextMapperName, "Added dex file: " + newDex);

        } catch (IOException ioe) {
            Log.e(contextMapperName, ioe.getStackTrace().toString());

        }
    }*/

    public void copyDexFile(String appKey, String classpackage, String classpackagemeta) {

        File newDexFile = new File(classpackage);

        String newDexFileName = newDexFile.getName();

        File dexInternalStoragePath = new File(mContext.getDir("dex",
                Context.MODE_PRIVATE), newDexFileName);

        BufferedInputStream bis = null;
        OutputStream dexWriter = null;

        final int BUF_SIZE = 4096;

        try {

            bis = new BufferedInputStream(new FileInputStream(newDexFile));
            dexWriter = new BufferedOutputStream(new FileOutputStream(
                    dexInternalStoragePath));

            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0 ) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();

            ClassPackage classPackage = ClassPackage.parseClassPackage(classpackagemeta);

            int size = classPackage.mObservers.length;

            if (size == classPackage.mObserverPermissions.length) {

                for (int i=0; i < size; i++) {

                    String fullpath = classPackage.mObservers[i];
                    String classname = fullpath.substring(fullpath.lastIndexOf("."));
                    String packagename = fullpath.replace(classname, "");

                    mContextDatabase.insertObserver(packagename, classname, appKey, classPackage.mObserverPermissions[i], newDexFileName);
                }
            }

            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Added dex file: " + classpackage);

        } catch (IOException ioe) {
            Log.e(LOGTAG, ioe.getStackTrace().toString());

        }
    }

    protected ContextObserver loadContextClass(String appId, String componentName) {
        List<String> componentInfo = mContextDatabase.getLoadObserverInfo(appId,
                componentName);

        if (componentInfo != null) {
            if (componentInfo.size() > 0) {
                return loadContextClass(appId, componentName, componentInfo.get(0),
                        componentInfo.get(1));
            }
        }

        return null;

    }

    private ContextObserver loadContextClass(String appId, String componentName, String dex,
                              String packagename) {

        final File optimizedDexOutputPath = mContext.getDir("outdex",
                        MODE_PRIVATE);
        File dexInternalStoragePath;

        dexInternalStoragePath = new File(mContext.getDir("dex",
                        MODE_PRIVATE), dex);

        DexClassLoader cl = new DexClassLoader(
                        dexInternalStoragePath.getAbsolutePath(),
                        optimizedDexOutputPath.getAbsolutePath(), null,
                        mContext.getClassLoader());

        Class<?> contextObserver  = null;
        Class<?>[] parameterTypes = { Context.class };

        try {
            //Load the Class
            contextObserver = cl.loadClass(packagename.concat("." + componentName));
            Constructor<?> contextConstructor = contextObserver.getConstructor(parameterTypes);
            ContextObserver context = (ContextObserver) contextConstructor
                    .newInstance(mContext);
            context.setContextReceivers(mContextReceivers);
            context.addRequiringApp(appId);


            mActiveContexts.put(componentName, context);

            return context;

        } catch (ClassNotFoundException cnfe) {
            mLogger.logError(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Observer: " + componentName + " class not found!");
            return null;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
            return null;
        }
    }

    private boolean loadAllOtherReceiverClasses() {

        int numOfReceivers = mContextDatabase.getNumberOfReceivers();

        for (int i = 0; i < numOfReceivers; i++) {
            List<String> classInfo = mContextDatabase.getContextReceiver(i+1);
            return loadReceiverClass(classInfo.get(2), classInfo.get(0), classInfo.get(1));
        }

        return false;
    }

    private boolean loadReceiverClass(String componentName, String dex,
                                     String packagename) {

        final File optimizedDexOutputPath = mContext.getDir("outdex",
                MODE_PRIVATE);
        File dexInternalStoragePath;

        dexInternalStoragePath = new File(mContext.getDir("dex",
                MODE_PRIVATE), dex);

        DexClassLoader cl = new DexClassLoader(
                dexInternalStoragePath.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(), null,
                mContext.getClassLoader());

        Class<?> contextReceiver  = null;
        Class<?>[] parameterTypes = { IContextManager.class, IReasonerManager.class};

        try {
            //Load the Class
            contextReceiver = cl.loadClass(packagename.concat("." + componentName));
            Constructor<?> receiverConstructor = contextReceiver.getConstructor(parameterTypes);
            ContextReceiver receiver = (ContextReceiver) receiverConstructor
                    .newInstance(this, mReasonerCore.getReasonerManager());

            mContextReceivers.add(packagename, receiver);
            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Loaded Receiver: " + componentName);

            return true;

        } catch (ClassNotFoundException cnfe) {
            mLogger.logError(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Receiver: " + componentName + " class not found!");
            return false;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
            return false;
        }
    }

    public boolean needHouseClearing(){

        SharedPreferences settings = mContext.getSharedPreferences(Prefs.REASONER_PREFS, 0);
        String lastDateString = settings.getString("lastclearing", "");

        if (! lastDateString.equals("") ) {
            try {
                Date lastTime = mDateFormater.parse(lastDateString);
                Date today = mCalendar.getTime();

                int m1 = lastTime.getYear() * 12 + lastTime.getMonth();
                int m2 = lastTime.getYear() * 12 + lastTime.getMonth();

                if (m2 - m1 == 0) {
                    return false;
                } else {
                    return true;
                }
            } catch (ParseException e) {
                Log.e(LOGTAG, e.getStackTrace().toString());
                return false;
            }
        } else {
            return false;
        }
    }

    public void houseClearing() {



        SharedPreferences settings = mContext.getSharedPreferences(Prefs.REASONER_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastclearing", mDateFormater.format(mCalendar.getTime()));
        editor.commit();

    }

    public void newExternalContextValue(String name, long contextValue) {
        mContextReceivers.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, double contextValue) {
        mContextReceivers.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, boolean contextValue) {
        mContextReceivers.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, String contextValue) {
        mContextReceivers.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(String name, Object contextValue) {
        mContextReceivers.newContextValue(name, contextValue);
    }

    public void newExternalContextValue(Map<String, String> values) {
        mContextReceivers.newContextValues(values);
    }

    public boolean stop() {
        for (ContextObserver co : mActiveContexts.values()) {
            co.stop();
            mActiveContexts.remove(co);
            co.removeAllRequiringApps();
            co = null;
        }

        mContext.unregisterReceiver(mExternalContextReceiver);
        return true;
    }

    public boolean setContextParameters(String appkey, String observerName, Map parameters) {

        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer == null) {
            return false;
        } else {
            return observer.setContextParameters((HashMap<String, Object>) parameters);
        }
    }


    public boolean addObserverRequirementWithParameters
            (String appkey, String observerName, Map parameters) {
        ContextObserver observer = mActiveContexts.get(observerName);

        if (observer != null) {
            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER,
                    LOGTAG, "Observer " + observerName + " already running, adding requirement");
            observer.addRequiringApp(appkey);
            return true;
        } else {
            observer = loadContextClass(appkey, observerName);

            if (observer == null) {
                mLogger.logError(DataLogger.CONTEXT_MANAGER,
                        LOGTAG, "Observer " + observerName + " could not be loaded");
                return false;
            } else {
                observer.setContextParameters((HashMap<String, Object>) parameters);

                if (! observer.start()) {
                    observer.removeAllRequiringApps();
                    mActiveContexts.remove(observerName);
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public void sendContextUpdate(String contextName, String contextValue) {

        mReasonerCore.sendContextResult(contextName, contextValue);
    }

    @Override
    public void updateContextValue(String contextName, String value) {
        mReasonerCore.updateAtomicContext(contextName, value);
    }
}
