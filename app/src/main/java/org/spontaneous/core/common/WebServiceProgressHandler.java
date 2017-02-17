package org.spontaneous.core.common;

/**
 * Interface for handling the progress while calling a webservice.
 *
 * @author Dominik Dzienia
 */
public interface WebServiceProgressHandler {
    public void showWebServiceProgress();

    public void hideWebServiceProgress();
}
