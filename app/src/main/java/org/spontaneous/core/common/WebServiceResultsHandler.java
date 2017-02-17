package org.spontaneous.core.common;

/**
 * An interface for handling results of calling a webservice. It handles technical & business errors,
 * authorization problems and also the proceeding if the request was successful.
 *
 * @author Dominik Dzienia
 */
public interface WebServiceResultsHandler {

    public abstract void onAnyResult(WebServiceResponse response);

    public abstract void onResponseSuccessful(WebServiceResponse response);

    public abstract void onResponseUnauthorized(WebServiceResponse response);

    public abstract void onError(WebServiceResponse response);

    public abstract void onBusinessError(WebServiceResponse response);

}