package org.spontaneous.core.common;

import android.content.Context;
import android.util.Log;

import org.spontaneous.core.ws.clients.RESTEndpointTask;

public abstract class WebServiceCallHandler implements WebServiceHandler {

    private RESTEndpointTask task = null;
    GenericWebService webService = null;
    WebServiceHandler defaultHandlerProxy = null;

    public WebServiceCallHandler()
    {
    }

    public WebServiceCallHandler(WebServiceHandler defaulthandler)
    {
        this.defaultHandlerProxy = defaulthandler;
    }

    public void setTask(RESTEndpointTask restEndpointTask)
    {
        this.task = restEndpointTask;
        this.task.setProgressHandler(this);
        this.task.setResultsHandler(this);
    }

    public void setService(GenericWebService webService)
    {
        this.webService = webService;

    }

    public RESTEndpointTask getTask()
    {
        return task;
    }

    public void clear()
    {
        this.task = null;
    }

    public void cancel(boolean mayInterruptIfRunning)
    {
        if (this.task != null) {
            this.task.cancel(mayInterruptIfRunning);
        }
    }

    public boolean isTaskInProgress()
    {
        return ((this.task != null) && (!this.task.isCancelled()));
    }

    public GenericWebService getWebService()
    {
        return webService;
    }

    @Override
    public void showWebServiceProgress()
    {
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.showWebServiceProgress();
        }
    }

    @Override
    public void hideWebServiceProgress()
    {
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.hideWebServiceProgress();
        }
    }

    @Override
    public void onResponseSuccessful(WebServiceResponse response)
    {
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.onResponseSuccessful(response);
        }
        clear();

    }

    @Override
    public void onResponseUnauthorized(WebServiceResponse response)
    {
        clear();
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.onResponseUnauthorized(response);
        }

    }

    @Override
    public void onError(WebServiceResponse response)
    {
        Log.i("WebService", "onError #" + response.getError().getCode() + " : " + response.getError().getMessage());
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.onError(response);
        }
        clear();
    }

    @Override
    public void onBusinessError(WebServiceResponse response)
    {
        Log.i("WebService", "onBusinessError #" + response.getError().getCode() + " : " + response.getError().getMessage());
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.onBusinessError(response);
        }
        clear();
    }

    @Override
    public void onAnyResult(WebServiceResponse response)
    {
        if (defaultHandlerProxy != null) {
            defaultHandlerProxy.onAnyResult(response);
        }
    }

    abstract public Context getContext();


}