package org.spontaneous.core.common.error;

public enum ErrorCategory {
    OTHER,
    INTERNAL, // app internal errors
    NETWORK,  // network related, technical errors
    CLIENT,   // reported by WebService, client related errors
    BACKEND,  // backend errors (business and technical)
    AUTH,     // authentication related errors
    USER      // error triggered by user
}