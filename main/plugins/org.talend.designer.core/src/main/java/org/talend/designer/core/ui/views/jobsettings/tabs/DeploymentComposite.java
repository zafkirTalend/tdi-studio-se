// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.views.jobsettings.tabs;

import static org.talend.repository.utils.MavenVersionUtils.containsKey;
import static org.talend.repository.utils.MavenVersionUtils.get;
import static org.talend.repository.utils.MavenVersionUtils.getDefaultVersion;
import static org.talend.repository.utils.MavenVersionUtils.isAdditionalPropertiesNull;
import static org.talend.repository.utils.MavenVersionUtils.isValidMavenVersion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.process.TalendProcessArgumentConstant;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.runtime.repository.build.BuildType;
import org.talend.core.runtime.repository.build.IBuildParametes;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.MavenDeploymentValueChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;

public class DeploymentComposite extends AbstractTabComposite {

    Color COLOR_RED = getDisplay().getSystemColor(SWT.COLOR_RED);

    private Button groupIdCheckbox;

    private Text groupIdText;

    private Button versionCheckbox;

    private Text versionText;

    private Button snapshotCheckbox;

    private Label buildTypeLabel;

    private ComboViewer buildTypeCombo;

    private String defaultGroupId;

    private String groupId;

    private String defaultVersion;

    private String version;

    private Process process;

    private Item serviceItem;

    private CommandStack commandStack;

    private IESBService esbService;

    private boolean isService;

    private boolean isDataServiceJob; // Is ESB SOAP Service Job

    public DeploymentComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            IRepositoryViewObject repositoryViewObject) {
        super(parent, style, widgetFactory, repositoryViewObject);
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            esbService = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
        }
        if (repositoryViewObject instanceof Process) {
            process = (Process) repositoryViewObject;
            commandStack = process.getCommandStack();
            defaultVersion = getDefaultVersion(process.getVersion());

            isDataServiceJob = false;
            // Disable widgests in case of the job is for ESB data service
            if (!process.getComponentsType().equals(ComponentCategory.CATEGORY_4_CAMEL.getName())) {
                List<INode> nodes = (List<INode>) process.getGraphicalNodes();
                for (INode node : nodes) {
                    if ("tESBProviderRequest".equals(node.getComponent().getName())) {
                        isDataServiceJob = true;
                        defaultVersion = "";
                        break;
                    }
                }
            }
        } else {
            IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (esbService.isWSDLEditor(editor) && esbService.getServicesType() == repositoryViewObject.getRepositoryObjectType()) {
                serviceItem = esbService.getWSDLEditorItem(editor);
                commandStack = (CommandStack) editor.getAdapter(CommandStack.class);
                defaultVersion = getDefaultVersion(serviceItem.getProperty().getVersion());
                isService = true;
            }
        }
        createControl();
        initialize();
        addListeners();
        checkReadOnly();
    }

    private void checkReadOnly() {
        try {
            String currentVersion = isService ? serviceItem.getProperty().getVersion() : process.getVersion();
            IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion(
                    isService ? serviceItem.getProperty().getId() : process.getId());
            String latestVersion = obj.getVersion();

            if (!currentVersion.equals(latestVersion) || isDataServiceJob) {
                groupIdCheckbox.setEnabled(false);
                groupIdText.setEnabled(false);
                versionCheckbox.setEnabled(false);
                versionText.setEnabled(false);
                snapshotCheckbox.setEnabled(false);
                if (buildTypeCombo != null) {
                    buildTypeCombo.getCCombo().setEnabled(false);
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

    }

    private void createControl() {
        setLayout(new GridLayout());
        setBackground(getParent().getBackground());
        if (isDataServiceJob) {
            Composite messageComposite = new Composite(this, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.horizontalSpacing = 10;
            layout.verticalSpacing = 10;
            messageComposite.setLayout(layout);
            messageComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            widgetFactory.createLabel(messageComposite,
                    "SOAP data service cannot be published, deployment setting is \naccording to the defined service.");
        }
        Composite composite = new Composite(this, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        groupIdCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.gourpIdLabel"), //$NON-NLS-1$
                SWT.CHECK);
        groupIdCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        groupIdText = new Text(composite, SWT.BORDER);
        GridData groupIdTextData = new GridData(GridData.FILL_HORIZONTAL);
        groupIdTextData.widthHint = 200;
        groupIdText.setLayoutData(groupIdTextData);

        versionCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.versionLabel"), //$NON-NLS-1$
                SWT.CHECK);
        versionCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        versionText = new Text(composite, SWT.BORDER);
        GridData versionTextData = new GridData(GridData.FILL_HORIZONTAL);
        versionTextData.widthHint = 200;
        versionText.setLayoutData(versionTextData);

        snapshotCheckbox = widgetFactory.createButton(composite, Messages.getString("DeploymentComposite.snapshotLabel"), //$NON-NLS-1$
                SWT.CHECK);
        GridData snapshotCheckboxData = new GridData(GridData.FILL_HORIZONTAL);
        snapshotCheckboxData.horizontalSpan = 2;
        snapshotCheckbox.setLayoutData(snapshotCheckboxData);

        buildTypeLabel = widgetFactory.createLabel(composite, Messages.getString("DeploymentComposite.buildTypeLabel")); //$NON-NLS-1$
        buildTypeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        buildTypeCombo = new ComboViewer(widgetFactory.createCCombo(composite, SWT.READ_ONLY | SWT.BORDER));
        final Control buildTypeControl = buildTypeCombo.getControl();
        GridData buildTypeComboData = new GridData(GridData.FILL_HORIZONTAL);
        buildTypeComboData.widthHint = 200;
        buildTypeControl.setLayoutData(buildTypeComboData);
        buildTypeCombo.setContentProvider(ArrayContentProvider.getInstance());
        buildTypeCombo.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof BuildType) {
                    return ((BuildType) element).getLabel();
                }
                return super.getText(element);
            }

        });

    }

    private void initialize() {
        if (!isAdditionalPropertiesNull(getObject())) {
            // TODO get from PublishPlugin.getDefault().getPreferenceStore();
            defaultGroupId = isDataServiceJob ? "" : "org.example"; // $NON-NLS-1$
            if (groupId == null) {
                groupId = (String) get(getObject(), MavenConstants.NAME_GROUP_ID);
                if (groupId == null) {
                    groupId = defaultGroupId;
                }
            }
            if (groupId != null) {
                boolean isDefaultGroupId = groupId.equals(defaultGroupId);
                groupIdCheckbox.setSelection(!isDefaultGroupId);
                groupIdText.setEnabled(!isDefaultGroupId);
                groupIdText.setText(groupId);
            } else {
                groupIdText.setText(defaultGroupId);
                groupIdCheckbox.setSelection(false);
                groupIdText.setEnabled(false);
            }
            if (version == null) {
                version = (String) get(getObject(), MavenConstants.NAME_USER_VERSION);
                if (version == null) {
                    version = defaultVersion;
                }
            }
            if (version != null) {
                boolean isDefaultVersion = version.equals(defaultVersion);
                versionCheckbox.setSelection(!isDefaultVersion);
                versionText.setEnabled(!isDefaultVersion);
                versionText.setText(version);
                versionText.setToolTipText(""); //$NON-NLS-1$
            } else {
                versionCheckbox.setSelection(false);
                versionText.setEnabled(false);
                versionText.setText(defaultVersion);
                versionText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$ ;
            }

            boolean useSnapshot = containsKey(getObject(), MavenConstants.NAME_PUBLISH_AS_SNAPSHOT);
            snapshotCheckbox.setSelection(useSnapshot);

            final boolean showBuildType = isShowBuildType();
            final Control buildTypeControl = buildTypeCombo.getControl();
            buildTypeControl.setVisible(showBuildType);
            buildTypeLabel.setVisible(showBuildType);

            if (showBuildType) {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(getObjectType(), getObject());
                final BuildType[] validBuildTypes = BuildExportManager.getInstance().getValidBuildTypes(parameters);
                buildTypeCombo.setInput(validBuildTypes);
                buildTypeControl.setEnabled(true);
                String buildType = (String) get(getObject(), TalendProcessArgumentConstant.ARG_BUILD_TYPE);
                BuildType foundType = null;
                if (buildType != null) {
                    for (BuildType t : validBuildTypes) {
                        if (t.getName().equals(buildType)) {
                            foundType = t;
                            break;
                        }
                    }
                }
                if (foundType == null) {// set the first one by default
                    foundType = validBuildTypes[0];
                }
                buildTypeCombo.setSelection(new StructuredSelection(foundType));
            }
        }
    }

    private boolean isShowBuildType() {
        // TODO need to add support for ESB Service.
        if (!PluginChecker.isTIS() || isService) {
            return false;
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(getObjectType(), getObject());
        final BuildType[] validBuildTypes = BuildExportManager.getInstance().getValidBuildTypes(parameters);
        if (validBuildTypes != null && validBuildTypes.length > 1) {// TUP-17276
            return true;
        }
        return false;
    }

    private void addListeners() {
        groupIdCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (groupIdCheckbox.getSelection()) {
                    groupIdText.setEnabled(true);
                    groupIdText.setText(groupId);
                } else {
                    groupIdText.setEnabled(false);
                    groupIdText.setText(defaultGroupId);
                    // remove key, so will be default groupId
                    Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_GROUP_ID, null);
                    getCommandStack().execute(cmd);
                }
            }

        });

        groupIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String currentGroupId = groupIdText.getText();
                if (currentGroupId != null && !currentGroupId.trim().equals("")) { //$NON-NLS-1$
                    groupIdText.setBackground(getBackground());
                    groupIdText.setToolTipText(""); //$NON-NLS-1$
                    if (!defaultGroupId.equals(currentGroupId)) {
                        groupId = currentGroupId;
                    } else {
                        currentGroupId = null;
                    }
                    Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_GROUP_ID, currentGroupId);
                    getCommandStack().execute(cmd);
                } else {
                    groupIdText.setBackground(COLOR_RED);
                    groupIdText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$
                }
            }
        });

        versionCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (versionCheckbox.getSelection()) {
                    versionText.setEnabled(true);
                    versionText.setText(version);
                } else {
                    versionText.setEnabled(false);
                    versionText.setText(defaultVersion);
                    // remove key, so will be default version
                    Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_USER_VERSION, null);
                    getCommandStack().execute(cmd);
                }
            }

        });

        versionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String currentVersion = versionText.getText();
                if (currentVersion != null && !currentVersion.trim().equals("") //$NON-NLS-1$
                        && !isValidMavenVersion(currentVersion, snapshotCheckbox.getSelection())) {
                    versionText.setToolTipText(Messages.getString("DeploymentComposite.valueWarning")); //$NON-NLS-1$
                    versionText.setBackground(COLOR_RED);
                } else {
                    versionText.setToolTipText(""); //$NON-NLS-1$
                    versionText.setBackground(getBackground());
                    if (!defaultVersion.equals(currentVersion)) {
                        version = currentVersion;
                    } else {
                        currentVersion = null;
                    }
                    // if empty, remove it from job, else will set the new value
                    Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_USER_VERSION,
                            currentVersion);
                    getCommandStack().execute(cmd);
                }
            }

        });

        snapshotCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // if unchecked then remove key.
                String useSnapshot = snapshotCheckbox.getSelection() ? String.valueOf(true) : null;
                Command cmd = new MavenDeploymentValueChangeCommand(getObject(), MavenConstants.NAME_PUBLISH_AS_SNAPSHOT,
                        useSnapshot);
                getCommandStack().execute(cmd);
            }

        });

        buildTypeCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final ISelection selection = event.getSelection();
                if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                    final Object elem = ((IStructuredSelection) selection).getFirstElement();
                    if (elem instanceof BuildType) {
                        Command cmd = new MavenDeploymentValueChangeCommand(getObject(),
                                TalendProcessArgumentConstant.ARG_BUILD_TYPE, ((BuildType) elem).getName());
                        getCommandStack().execute(cmd);
                    }
                }
            }

        });
    }

    private CommandStack getCommandStack() {
        return commandStack;
    }

    private Object getObject() {
        return isService ? serviceItem.getProperty() : process;
    }

    private String getObjectType() {
        return isService ? IBuildParametes.SERVICE : IBuildParametes.PROCESS;
    }

}
