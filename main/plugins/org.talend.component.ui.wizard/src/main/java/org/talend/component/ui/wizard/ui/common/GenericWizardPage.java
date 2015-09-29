// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.component.ui.wizard.ui.common;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.properties.ConnectionItem;

/**
 * 
 * created by ycbai on 2015年9月29日 Detailled comment
 *
 */
public abstract class GenericWizardPage extends WizardPage {

    protected ConnectionItem connectionItem;

    protected String[] existingNames;

    protected boolean isRepositoryObjectEditable;

    protected Form form;

    protected ComponentService compService;

    public GenericWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, String[] existingNames,
            boolean creation, Form form, ComponentService compService) {
        super("GenericWizardPage"); //$NON-NLS-1$
        this.connectionItem = connectionItem;
        this.existingNames = existingNames;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
        this.form = form;
        this.compService = compService;
    }

    private boolean callBefore() {
        if (form.isCallBeforeFormPresent()) {
            try {
                compService.beforeFormPresent(form.getName(), form.getProperties());
                return true;
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    protected FormData createFormData() {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        return data;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            callBefore();
        }
        setPageComplete(visible);
    }

    public Form getForm() {
        return this.form;
    }

}
