/*
 * $Id: ServiceValidationException.java,v 1.2 2004/02/19 18:52:35 ajzeneski Exp $
 *
 * Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.service;

import java.util.List;
import java.util.ArrayList;

/**
 * ServiceValidationException
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.2 $
 * @since      2.0
 */
public class ServiceValidationException extends GenericServiceException {

    protected List missingFields = new ArrayList();
    protected List extraFields = new ArrayList();
    protected ModelService service = null;

    protected ServiceValidationException() {
        super();
    }

    public ServiceValidationException(ModelService service, List missingFields, List extraFields) {
        super();
        this.service = service;
        if (missingFields != null) {
            this.missingFields = missingFields;
        }
        if (extraFields != null) {
            this.extraFields = extraFields;
        }
    }

    protected ServiceValidationException(String str) {
        super(str);
    }

    public ServiceValidationException(String str, ModelService service, List missingFields, List extraFields) {
        super(str);
        this.service = service;
        if (missingFields != null) {
            this.missingFields = missingFields;
        }
        if (extraFields != null) {
            this.extraFields = extraFields;
        }
    }

    protected ServiceValidationException(String str, Throwable nested) {
        super(str, nested);
    }

    public ServiceValidationException(String str, Throwable nested, ModelService service, List missingFields, List extraFields) {
        super(str, nested);
        this.service = service;
        if (missingFields != null) {
            this.missingFields = missingFields;
        }
        if (extraFields != null) {
            this.extraFields = extraFields;
        }
    }

    public List getExtraFields() {
        return extraFields;
    }

    public List getMissingFields() {
        return missingFields;
    }

    public ModelService getModelService() {
        return service;
    }

    public String getServiceName() {
        if (service != null) {
            return service.name;
        } else {
            return null;
        }
    }
}

