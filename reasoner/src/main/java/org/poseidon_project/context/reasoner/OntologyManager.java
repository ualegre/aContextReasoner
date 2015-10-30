/*
 * Copyright 2015 POSEIDON Project
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

package org.poseidon_project.context.reasoner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.database.ContextDB;
import org.poseidon_project.context.database.ContextResult;
import org.poseidon_project.context.logging.DataLogger;
import org.poseidon_project.context.utility.FileOperations;
import org.poseidon_project.contexts.IOntologyManager;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.SatSolver;
import org.sat4j.specs.TimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * Class to manage the extensible POSEIDON ontology
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class OntologyManager implements IOntologyManager{

    private long TIMEOUT = 5000;
    private Context mContext;
    private OntModel mModel;
    private static final String LOGTAG  = "OntologyManager";
    private static final String ONTOLOGY_PREFS = "OntologyPrefs";
    private ContextReasonerCore mReasonerCore;
    private CsparqlEngine mCsparqlEngine;
    private CsparqlQueryResultProxy mCsparqlQRP;
    private ContextStream mContextStream;
    private ContextRuleObserver mContextRuleObserver;
    private DataLogger mLogger;
    //Only required for the pilot until the main infrastructure is done.
    public ContextMapper pilotMapper;
    private HashMap<String, Object> mOntIndividuals;
    private HashMap<String, AggregateRule> mAggregateRules;
    private ContextDB mContextDatabase;

    public OntologyManager(Context context, ContextReasonerCore core, ContextDB db){
        mContext = context;
        mReasonerCore = core;
        mLogger = core.getLogger();
        mAggregateRules = new HashMap<>();
        mContextDatabase = db;

        /* Not currently used, pointless loading unless we use this.
        runFirstTime();

        mModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        loadMappingFiles();
        loadOntologies();
        */

        startCSPARQL();

        //Not a completely bad idea to do a GC after loading everything
        System.gc();

        pilotMapper = new ContextMapper(mReasonerCore, this);
    }

    private void startCSPARQL() {

        mCsparqlEngine = new CsparqlEngineImpl();

        mCsparqlEngine.initialize(true);
        mContextStream = new ContextStream("http://poseidon-project.org/context-stream");

        mCsparqlEngine.registerStream(mContextStream);
        mContextRuleObserver = new ContextRuleObserver(mReasonerCore);
    }

    public CsparqlQueryResultProxy registerCSPARQLQuery(String query) {

        CsparqlQueryResultProxy queryResultProxy = null;
        try {
            queryResultProxy = mCsparqlEngine.registerQuery(query, true);
            queryResultProxy.addObserver(mContextRuleObserver);

        } catch (final ParseException e) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG, "Cannot parse: " + query);
            Log.e(LOGTAG, "Error Parsing: " + e.getMessage());
        }

        return queryResultProxy;
    }

    public void unregisterCSPARQLQuery(String id) {
        mCsparqlEngine.unregisterQuery(id);
    }

    private void loadMappingFiles() {

        try {
            //Lets open POSEIDONs first, then deal with others.
            parseURLtoFileMappingFile(mContext.getAssets().open("ontologyMap.json"));




        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean loadOntologies() {

        //Read and Open all POSEIDON Related Ontologies
        if (mModel == null) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG,
                    "Model not initialised, ignoring");
            return false;
        } else {
            for (String uri : POSEIDONOntologies.ONTOLOGIES_ARRAY) {
                mModel.read(uri);
            }
            return true;
        }

    }

    private void runFirstTime() {
        SharedPreferences settings = mContext.getSharedPreferences(ONTOLOGY_PREFS, 0);

        boolean beenRun = settings.getBoolean("ranFirst", false);

        if (! beenRun ) {
            try {

                String existingDirString = Environment.
                        getExternalStorageDirectory().getAbsolutePath() + "/ontologies";

                File existingDir = new File(existingDirString);

                FileOperations.deleteDirectory(existingDir);


                InputStream in = mContext.getAssets().open("ontologyMap.json");

                OntologyFileMapParser parser = new OntologyFileMapParser(in);
                HashMap<String, String> toBeCopied = parser.parse();

                for(String filepath : toBeCopied.values()) {
                    boolean copied = copyOntologyFile(filepath);

                    if (! copied ){
                        mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG,
                                "Failed to copy to SD Card: " + filepath);
                    }
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("ranFirst", true);

            } catch (IOException e) {
                Log.e(LOGTAG, e.getStackTrace().toString());
            }
        }

    }

    private boolean copyOntologyFile(String filepath) {

        try {
            String filename = filepath.substring(filepath.lastIndexOf("/") + 1);

            InputStream in = mContext.getAssets().open(filename);

            FileOperations.copyFile(in, filepath);

        } catch (IOException e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
            return false;
        }

        return true;
    }

    public void loadOntologyFromFile(OntModel model, String location) {
        try {
            File ontFile = new File(location);
            InputStream in = new FileInputStream(ontFile);
            model.read(in, null);
            in.close();
        } catch (IOException e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
        }
    }

    public void loadOntologyFromURL(OntModel model, String url) {
        model.read(url);
    }

    public void parseURLtoFileMappingFile(String location) {
        try {
            File mappingFile = new File(location);
            InputStream in = new FileInputStream(mappingFile);
            parseURLtoFileMappingFile(in);
        } catch(IOException e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
        }

    }

    public boolean parseURLtoFileMappingFile(InputStream in) {

        boolean result = true;
        OntologyFileMapParser parser = new OntologyFileMapParser(in);
        HashMap<String, String> toBeMapped = parser.parse();

        for(Map.Entry<String, String> entry : toBeMapped.entrySet()) {
            if (! mapOntologyURLtoFile(entry.getKey(), entry.getValue()) ) {
                result = false;
            }
        }

        return result;

    }

    public boolean mapOntologyURLtoFile(String url, String fileLocation) {

        if (mModel == null) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG, "Model not initialised, ignoring");
            return false;
        } else {
            OntDocumentManager dm = mModel.getDocumentManager();
            dm.addAltEntry(url, "file:" + fileLocation);
            return true;
        }

    }


    public boolean runSPARQLQuery(String queryText) {

        if (mModel == null) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG, "Model not initialised, ignoring");
            return false;
        } else {
            runSPARQLQuery(queryText, mModel);
            return true;
        }

    }

    public void runSPARQLQuery(String queryText, OntModel model) {

        Query query = QueryFactory.create(queryText);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet results = queryExecution.execSelect();

        //close QueryExecution
        queryExecution.close();

    }

    public void updatePropertyValue(String property, String value) {

    }

    @Override
    public void updateValues(String subject, String predicate, String value) {
        mContextStream.sendStream(subject, predicate, value, System.currentTimeMillis());
    }

    public void updateValues(String subject, String predicate, String value, long time) {
        mContextStream.sendStream(subject, predicate, value, time);
    }

    public void stop() {
        mCsparqlEngine.destroy();
    }

    public void registerAggregateRule(String rule) {

        long currentTime = System.currentTimeMillis();

        AggregateRule aggregateRule = new AggregateRule(rule);
        List<String> cachLiterals = aggregateRule.getCachibleLiterals();

        for (String needToCache : cachLiterals) {

            TemporalValue tvalue = aggregateRule.getTemporalValue(needToCache);

            if (tvalue != null) {
                aggregateRule.addCachedLiteral(
                        evaluateTemporalLiteral(needToCache, tvalue, currentTime));
            }

            mLogger.logError(DataLogger.REASONER, "Cachible Literal has no temporal value");
        }

        mAggregateRules.put("test", aggregateRule);
    }

    public synchronized void fireAggregateRules(String newContextValue) {

        long mCurrentTime = System.currentTimeMillis();

        for (AggregateRule rule : mAggregateRules.values()) {
            //Consider threadpool
            if (rule.isAffectedBy(newContextValue)) {
                fireRule(rule, mCurrentTime);
            }
        }
    }

    private void fireRule(AggregateRule rule, long mCurrentTime) {

        LinkedList<Node> literalValues = new LinkedList<Node>();

        //Lets get any cached temporal literals;
        literalValues.addAll(rule.getCachedLiterals());

        //Lets get all instance literals
        for (String instanceLiteral : rule.getInstanceLiterals()) {
            String contextName = instanceLiteral.substring(0, instanceLiteral.indexOf("_"));

            ContextResult cr = mReasonerCore.mContextValues.get(contextName);
            Literal literal = new Literal(instanceLiteral, false);

            if (cr == null) {

            } else {
                if (cr.getFullName().equals(instanceLiteral)) {
                    literal.flip();
                } else {

                }
            }

            literalValues.add(literal);

        }

        //Lets get all non-cached Temporal literals
        for (Map.Entry<String, TemporalValue> temporalLiteral : rule.getTemporalLiterals().entrySet()) {
                literalValues.add(evaluateTemporalLiteral(
                        temporalLiteral.getKey(), temporalLiteral.getValue(), mCurrentTime));
        }

        try {
            SatSolver solver = new SatSolver(rule.getPropNodes(), TIMEOUT);

            boolean result = solver.isSatisfiable(literalValues);
        } catch (TimeoutException exception) {
            exception.printStackTrace();
        }
    }

    private Literal evaluateTemporalLiteral(String literalName, TemporalValue literalValue,
                                            long currentTime) {

        Literal literal = new Literal(literalName, false);

        if (literalValue.mAbsolute) {
            //check db
            literal.positive = mContextDatabase.
                    contextValuePresentAbsolute(literalName, literalValue.mStartTime,
                            literalValue.mEndTime, literalValue.mStrong);
        } else {
            String contextName = literalName.substring(0, literalName.indexOf("_"));

            ContextResult cr = mReasonerCore.mContextValues.get(contextName);

            if (cr != null) {
                if (cr.getFullName().equals(literalName)) {

                    long diff = currentTime - cr.getContextTime();

                    if (diff > literalValue.mStartTime) {
                        literal.positive = true;
                    }
                }
            } else {
                //check db
                literal.positive = mContextDatabase.
                        contextValuePresentRelative(literalName, currentTime - literalValue.mStartTime,
                                literalValue.mStrong);
            }

        }

        return literal;
    }

}
