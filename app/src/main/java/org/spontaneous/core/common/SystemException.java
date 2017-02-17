package org.spontaneous.core.common;

/**
 * Standard exception used by WebServices, but only for fatal, non recoverable,
 * and early stage errors.
 *
 * WebServices introduces whole system of capturing errors and other backend
 * related communication, encapsulating it already in WebServiceResponse
 * error related fields.
 *
 * @author Dominik Dzienia
 */
public class SystemException extends Exception {

    public SystemException(String msg)
    {
        super(msg);
    }

    private static final long serialVersionUID = -4630346819817271180L;

}
