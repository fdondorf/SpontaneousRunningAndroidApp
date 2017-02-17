package org.spontaneous.core.common;

import org.spontaneous.core.ws.clients.RESTEndpointTask;

/**
 * Generic webservice for asynchronous requests.
 *
 * Created by rstroh on 23.11.2015.
 */
public abstract class GenericAsyncWebservice extends GenericWebService {

    /**
     * Method configures request with given configuration and executes Request-Task asynchronous.
     *
     * @param requestResult webservice call handler
     * @param req           Configuration for webservice request
     */
    protected void configureAndExecuteRequest(WebServiceCallHandler requestResult, WebServiceRequestConfig req)
    {
        requestResult.setService(this);
        requestResult.setTask(new RESTEndpointTask(req));
        requestResult.getTask().execute();
    }

    public void interpretResponse(WebServiceResponse response)
    {
    }

    /**
     * Builds, configures and executes request.
     *
     * @param requestResult webservice call handler
     * @throws SystemException Standard-Exception for webservices
     */
    public abstract void doRequest(WebServiceCallHandler requestResult) throws SystemException;
}
