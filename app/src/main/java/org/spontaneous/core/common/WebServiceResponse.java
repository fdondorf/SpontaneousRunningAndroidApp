package org.spontaneous.core.common;


import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.core.common.error.SystemError;


public class WebServiceResponse {

    public static enum Status {
        OK,
        ERROR,
        UNDEFINED;
    }

    public static enum ContentStatus {
        EMPTY,
        JSON,
        UNKNOWN
    }

    public static final String ERROR_PAYLOAD = MainActivity.class.getPackage() + ".error.payload";

    public static class Builder {

        private final WebServiceResponse instance = new WebServiceResponse();

        public Builder basedOn(WebServiceResponse template)
        {
            instance.cloneSetup(template);
            return this;
        }

        public Builder fail(SystemError error)
        {
            instance.setError(error);
            instance.setStatus(Status.ERROR);
            return this;
        }

        public Builder fail(SystemError error, String details)
        {
            error.setMessage(details);
            instance.setError(error);
            instance.setStatus(Status.ERROR);
            return this;
        }

        public Builder setContentStatus(ContentStatus contentStatus)
        {
            instance.setContentStatus(contentStatus);
            return this;
        }

        public WebServiceResponse build(WebServiceResponse targetInstance)
        {
            targetInstance.cloneSetup(instance);

            return targetInstance;
        }

        public WebServiceResponse build()
        {
            return instance;
        }

        public Builder success()
        {
            instance.setError(SystemError.NO_ERROR);
            instance.setStatus(Status.OK);
            return this;
        }

        public Builder setJSONContent(JSONObject jsonObject)
        {
            instance.setContentStatus(ContentStatus.JSON);
            instance.setJSONContent(jsonObject);
            return this;
        }

        public Builder decodeFromBrodcast(Intent intent)
        {
            String errorPalyoad = intent.getStringExtra(ERROR_PAYLOAD);
            SystemError err = SystemError.NO_ERROR;
            if (errorPalyoad != null) {
                err = SystemError.fromJSON(errorPalyoad);
            }
            instance.setError(err);
            return this;
        }

    }

    private Status status = Status.UNDEFINED;
    private SystemError error = SystemError.NO_ERROR;
    private ContentStatus contentStatus = ContentStatus.EMPTY;
    private JSONObject jsonContent = null;

    public Status getStatus()
    {
        return status;
    }

    public void cloneSetup(WebServiceResponse template)
    {
        this.status = template.status;
        this.error = template.error;
        this.contentStatus = template.contentStatus;
        this.jsonContent = template.jsonContent;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public SystemError getError()
    {
        return error;
    }

    public void setError(SystemError error)
    {
        this.error = error;
    }

    public ContentStatus getContentStatus()
    {
        return contentStatus;
    }

    public void setContentStatus(ContentStatus contentStatus)
    {
        this.contentStatus = contentStatus;
    }

    public JSONObject getJsonContent()
    {
        return jsonContent;
    }

    public void setJSONContent(JSONObject jsonObject)
    {
        this.jsonContent = jsonObject;
    }

    public boolean haveJsonContent()
    {
        return (jsonContent != null);
    }

    public void configureBroadcastWithError(Bundle additionals)
    {
        additionals.putString(ERROR_PAYLOAD, this.getError().toJSON());
    }

}
