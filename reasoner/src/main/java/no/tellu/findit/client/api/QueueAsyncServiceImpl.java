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

package no.tellu.findit.client.api;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Reimplentation of Tellu AsyncServiceImpl to handle multiple Transaction instances using a queue
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */

public class QueueAsyncServiceImpl implements AsyncService {

    private Queue<TransactionTask> mTasks = new ConcurrentLinkedQueue<>();
    private Queue<TaskData[]> mTaskData = new ConcurrentLinkedQueue<>();
    private ServiceSpec serviceSpec;
    private boolean isAuthenticated = false;
    private Thread mTransactionExecutioner;
    private QueueAsyncServiceImpl.TransactionTask mCurrentTask;
    private QueueAsyncServiceImpl.TaskData[] mCurrentTaskData;

    public QueueAsyncServiceImpl(String url) {
        this.serviceSpec = new ServiceSpec(url);
    }

    public QueueAsyncServiceImpl(String url, int connectTimeout, int readTimeout) {
        serviceSpec = new ServiceSpec(url, connectTimeout, readTimeout);
    }

    public void initiate(String username, String password, AccountCallback ac) {
        if(mTasks.isEmpty()) {
            this.isAuthenticated = false;
            this.serviceSpec.clearAccountData();
            this.serviceSpec.setAuthenticationToken((String)null);
            LoginTransaction trans = new LoginTransaction(username, password);
            mTasks.add(new QueueAsyncServiceImpl.LoginTask(ac));
            mTaskData.add(new QueueAsyncServiceImpl.TaskData[]{new QueueAsyncServiceImpl.TaskData(this.serviceSpec, trans)});
            executeTasks();
        } else {
            throw new IllegalStateException("Initiate must be called first");
        }
    }

    public void initiate(String authenticationToken, AccountCallback ac) {
        if(mTasks.isEmpty()) {
            this.isAuthenticated = false;
            this.serviceSpec.clearAccountData();
            this.serviceSpec.setAuthenticationToken(authenticationToken);
            this.serviceSpec.setUsername((String)null);
            AccountTransaction trans = new AccountTransaction();
            mTasks.add(new QueueAsyncServiceImpl.AccountTask(ac));
            mTaskData.add(new QueueAsyncServiceImpl.TaskData[]{new QueueAsyncServiceImpl.TaskData(this.serviceSpec, trans)});
            executeTasks();
        } else {
            throw new IllegalStateException("Initiate must be called first");
        }
    }

    public boolean isInitiated() {
        return this.isAuthenticated;
    }

    public void executeTasks() {

        if (mTransactionExecutioner == null) {
            mTransactionExecutioner = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (! mTasks.isEmpty()) {

                        mCurrentTask = mTasks.poll();
                        mCurrentTaskData = mTaskData.poll();

                        mCurrentTask.execute(mCurrentTaskData);

                        while(! (mCurrentTask.getStatus() == AsyncTask.Status.FINISHED)) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                Log.e("Thread", e.getMessage());
                            }
                        }
                    }

                    mTransactionExecutioner = null;
                }
            });

            mTransactionExecutioner.start();
        }
    }

    public void setCustomerContext(long customerId, AccountCallback ac) {

        this.serviceSpec.setCustomerId(customerId);
        AccountTransaction trans = new AccountTransaction();
        boolean noTransactions = mTasks.isEmpty();
        mTasks.add(new QueueAsyncServiceImpl.AccountTask(ac));
        mTaskData.add(new QueueAsyncServiceImpl.TaskData[]{new QueueAsyncServiceImpl.TaskData(this.serviceSpec, trans)});

        if (noTransactions) {
            executeTasks();
        }
    }

    public ServiceSpec getServiceSpec() {
            return this.serviceSpec;
    }

    public void doTransaction(ResourceTransaction trans, TransactionCallback tc) {
        boolean noTransactions = mTasks.isEmpty();
        mTasks.add(new QueueAsyncServiceImpl.TransactionTask(tc));
        mTaskData.add(new QueueAsyncServiceImpl.TaskData[]{new QueueAsyncServiceImpl.TaskData(this.serviceSpec, trans)});

        if (noTransactions) {
            executeTasks();
        }
    }

    public void doDataRetrieve(GetTransaction trans, DataRetrieveCallback drc) {

        boolean noTransactions = mTasks.isEmpty();
        mTasks.add(new QueueAsyncServiceImpl.GetTask(drc));
        mTaskData.add(new QueueAsyncServiceImpl.TaskData[]{new QueueAsyncServiceImpl.TaskData(this.serviceSpec, trans)});
        if (noTransactions) {
            executeTasks();
        }
    }

    public boolean isInProgress() {
        return ! mTasks.isEmpty();
    }

    public void abortTransaction() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
    }

    private class AccountTask extends QueueAsyncServiceImpl.TransactionTask {
        AccountCallback aCallback;

        AccountTask(AccountCallback ac) {
            super(null);
            this.aCallback = ac;
        }

        protected void onPostExecute(QueueAsyncServiceImpl.TaskData result) {
            QueueAsyncServiceImpl.this.serviceSpec = result.serviceSpec;
            if(this.aCallback != null) {
                if(result.transaction.getResponseCode() >= 200 && result.transaction.getResponseCode() < 300) {
                    QueueAsyncServiceImpl.this.isAuthenticated = true;
                    this.aCallback.onAccountOK(QueueAsyncServiceImpl.this.serviceSpec.getCustomerId(), QueueAsyncServiceImpl.this.serviceSpec.getCustomerName());
                } else {
                    this.aCallback.onAccountError(result.transaction.getResponseCode(), result.transaction.getError(), result.transaction.getException());
                }
            }
        }
    }

    private class GetTask extends QueueAsyncServiceImpl.TransactionTask {
        DataRetrieveCallback drCallback;

        GetTask(DataRetrieveCallback tc) {
            super(null);
            this.drCallback = tc;
        }

        protected void onPostExecute(QueueAsyncServiceImpl.TaskData result) {
            QueueAsyncServiceImpl.this.serviceSpec = result.serviceSpec;
            if(this.drCallback != null) {
                if(result.transaction.getResponseCode() >= 200 && result.transaction.getResponseCode() < 300) {
                    this.drCallback.onRetrieveOK(result.transaction.getResult(), (GetTransaction)result.transaction);
                } else {
                    this.drCallback.onRetrieveError(result.transaction.getResponseCode(), result.transaction.getError(), (GetTransaction)result.transaction);
                }
            }

        }
    }

    private class LoginTask extends QueueAsyncServiceImpl.TransactionTask {
        AccountCallback aCallback;

        LoginTask(AccountCallback ac) {
            super(null);
            this.aCallback = ac;
        }

        protected void onPostExecute(QueueAsyncServiceImpl.TaskData result) {
            if(result.serviceSpec.getAuthenticationToken() != null) {
                AccountTransaction trans = new AccountTransaction();

                new AccountTask(this.aCallback).execute(new QueueAsyncServiceImpl.TaskData[]{new TaskData(result.serviceSpec, trans)});
            } else {
                this.aCallback.onAccountError(result.transaction.getResponseCode(), result.transaction.getError(), result.transaction.getException());
            }

        }
    }

    private class TaskData {
        ServiceSpec serviceSpec;
        Transaction transaction;

        TaskData(ServiceSpec s, Transaction t) {
            this.serviceSpec = s;
            this.transaction = t;
        }
    }

    private class TransactionTask extends AsyncTask<QueueAsyncServiceImpl.TaskData, Void, QueueAsyncServiceImpl.TaskData> {
        TransactionCallback tCallback;

        TransactionTask(TransactionCallback tc) {
            this.tCallback = tc;
        }

        protected QueueAsyncServiceImpl.TaskData doInBackground(QueueAsyncServiceImpl.TaskData... params) {
            QueueAsyncServiceImpl.TaskData td = params[0];
            td.transaction.execute(td.serviceSpec);
            return td;
        }

        protected void onPostExecute(QueueAsyncServiceImpl.TaskData result) {
            QueueAsyncServiceImpl.this.serviceSpec = result.serviceSpec;
            if(this.tCallback != null) {
                if(result.transaction.getResponseCode() >= 200 && result.transaction.getResponseCode() < 300) {
                    if(result.transaction.getResult() == null) {
                        this.tCallback.onTransactionOK((JSONObject)null);
                    } else {
                        this.tCallback.onTransactionOK(result.transaction.getResult().optJSONObject(0));
                    }
                } else {
                    this.tCallback.onTransactionError(result.transaction.getResponseCode(), result.transaction.getError(), result.transaction.getException());
                }
            }

        }
    }
}
