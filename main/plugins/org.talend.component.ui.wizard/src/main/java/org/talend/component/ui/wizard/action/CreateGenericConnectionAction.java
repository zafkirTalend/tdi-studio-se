package org.talend.component.ui.wizard.action;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.talend.component.ui.model.genericMetadata.GenericConnectionItem;
import org.talend.component.ui.wizard.i18n.Messages;
import org.talend.component.ui.wizard.internal.service.GenericWizardInternalService;
import org.talend.component.ui.wizard.ui.GenericConnWizard;
import org.talend.component.ui.wizard.ui.common.GenericWizardDialog;
import org.talend.component.ui.wizard.util.GenericWizardServiceFactory;
import org.talend.component.ui.wizard.view.tester.GenericConnectionTester;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;

/**
 * 
 * created by ycbai on 2015年9月14日 Detailled comment
 *
 */
public class CreateGenericConnectionAction extends AbstractCreateAction {

    private static final int DEFAULT_WIZARD_WIDTH = 700;

    private static final int DEFAULT_WIZARD_HEIGHT = 400;

    private boolean creation = true;

    private GenericConnectionTester connectionTester;

    public CreateGenericConnectionAction() {
        super();
        if (repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }
        connectionTester = new GenericConnectionTester();
        this.setText(getCreateLabel());
        this.setToolTipText(getEditLabel());
        Image nodeImage = getNodeImage();
        if (nodeImage != null) {
            this.setImageDescriptor(ImageDescriptor.createFromImage(nodeImage));
        }
    }

    @Override
    protected void doRun() {
        IWizard wizard = new GenericConnWizard(PlatformUI.getWorkbench(), creation, repositoryNode, getExistingNames());
        WizardDialog wizardDialog = new GenericWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                wizard, new GenericWizardInternalService().getComponentService());
        if (Platform.getOS().equals(Platform.OS_LINUX)) {
            wizardDialog.setPageSize(getWizardWidth(), getWizardHeight() + 80);
        }
        wizardDialog.create();
        wizardDialog.open();
    }

    @Override
    protected void init(RepositoryNode node) {
        if (!connectionTester.isGenericConnection(node)) {
            setEnabled(false);
            return;
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                setEnabled(false);
                return;
            }
            if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                setEnabled(false);
                return;
            }
            this.setText(getCreateLabel());
            collectChildNames(node);
            creation = true;
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject()) && isLastVersion(node)) {
                this.setText(getEditLabel());
                collectSiblingNames(node);
            } else {
                this.setText(getOpenLabel());
            }
            creation = false;
            break;
        default:
            return;
        }
        setEnabled(true);
    }

    protected int getWizardWidth() {
        return DEFAULT_WIZARD_WIDTH;
    }

    protected int getWizardHeight() {
        return DEFAULT_WIZARD_HEIGHT;
    }

    protected String getCreateLabel() {
        return Messages.getString("CreateGenericConnectionAction.createLabel", getNodeLabel()); //$NON-NLS-1$
    }

    protected String getEditLabel() {
        return Messages.getString("CreateGenericConnectionAction.editLabel", getNodeLabel()); //$NON-NLS-1$
    }

    protected String getOpenLabel() {
        return Messages.getString("CreateGenericConnectionAction.openLabel", getNodeLabel()); //$NON-NLS-1$
    }

    protected String getNodeLabel() {
        return repositoryNode.getContentType().getLabel();
    }

    protected Image getNodeImage() {
        if (connectionTester.isGenericConnection(repositoryNode)) {
            return GenericWizardServiceFactory.getGenericWizardService().getNodeImage(repositoryNode.getContentType().getType());
        }
        return null;
    }

    @Override
    public Class getClassForDoubleClick() {
        return GenericConnectionItem.class;
    }

}
