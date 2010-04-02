// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process.jobsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.CorePlugin;
import org.talend.core.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants.ContextLoadInfo;
import org.talend.designer.core.model.process.statsandlogs.OracleComponentHelper;
import org.talend.designer.core.model.process.statsandlogs.StatsAndLogsManager;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.repository.model.ComponentsFactoryProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class JobSettingsManager {

    private static final String IMPLICIT_GROUP = "IMPLICIT_GROUP"; //$NON-NLS-1$

    public static void createJobSettingsParemeters(IProcess process) {
        // not used, only for hiding the table sql-builder button.
        // ElementParameter param;
        // List<IElementParameter> paramList = (List<IElementParameter>) process.getElementParameters();
        // param = new ElementParameter(process);
        // param.setName("MEMO_SQL"); //$NON-NLS-1$
        // param.setValue(""); //$NON-NLS-1$
        // param.setDisplayName("MEMO_SQL"); //$NON-NLS-1$
        // param.setField(EParameterFieldType.MEMO_SQL);
        // param.setCategory(EComponentCategory.EXTRA);
        // param.setNumRow(1);
        // param.setReadOnly(true);
        // param.setRequired(false);
        // param.setShow(false);
        // paramList.add(param);

        // for extra
        createExtraParameters(process);
        // for stats & logs
        boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
        if (!isJoblet) {
            StatsAndLogsManager.createStatsAndLogsParameters(process);
        }
        // for context
        createContextParameters(process);
    }

    private static final String CONTEXTLOAD_CONDITION = EParameterName.IMPLICIT_TCONTEXTLOAD.getName() + " == 'true'"; //$NON-NLS-1$

    private static final String QUOTE = TalendTextUtils.getQuoteChar();

    private static final String CONNECTOR = TalendTextUtils.getStringConnect();

    /**
     * 
     * create parameter for extra tab.
     */
    private static void createExtraParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) process.getElementParameters();

        // param = new ElementParameter(process);
        // param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.UPDATE_COMPONENTS.getName()));
        // param.setValue(Boolean.FALSE);
        // param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        // param.setField(EParameterFieldType.CHECK);
        // param.setCategory(EComponentCategory.EXTRA);
        // param.setNumRow(1);
        // param.setReadOnly(true);
        // param.setRequired(false);
        // param.setShow(false);
        // paramList.add(param);

        // use project settings
        param = new ElementParameter(process);
        param.setName(EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getDisplayName());
        param.setField(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName());
        param.setValue(Boolean.FALSE);
        param.setDisplayName(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getDisplayName());
        param.setField(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.STATSANDLOGS);
        param.setNumRow(2);
        param.setShow(false);
        paramList.add(param);
        // end

        boolean isJoblet = AbstractProcessProvider.isExtensionProcessForJoblet(process);
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();

        param = new ElementParameter(process);
        param.setName(EParameterName.MULTI_THREAD_EXECATION.getName());
        param.setValue(preferenceStore.getBoolean(ITalendCorePrefConstants.RUN_IN_MULTI_THREAD));
        param.setDisplayName(EParameterName.MULTI_THREAD_EXECATION.getDisplayName());
        param.setField(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(2);
        param.setShow(!isJoblet);
        param.setShowIf("(MULTI_THREAD_EXECATION=='true' or MULTI_THREAD_EXECATION=='false')"); //$NON-NLS-1$
        paramList.add(param);

        if (PluginChecker.isTIS()) {
            param = new ElementParameter(process);
            param.setName(EParameterName.PARALLELIZE_UNIT_SIZE.getName());
            param.setValue("25000"); //$NON-NLS-1$
            param.setDisplayName(EParameterName.PARALLELIZE_UNIT_SIZE.getDisplayName());
            param.setField(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(2);
            param.setShow(!isJoblet);
            paramList.add(param);
        }
        // achen modify to fix bug 0006241
        if (isJoblet) {
            param = new ElementParameter(process);
            param.setName(EParameterName.STARTABLE.getName());
            param.setValue(false);
            param.setDisplayName(EParameterName.STARTABLE.getDisplayName());
            param.setField(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(2);
            paramList.add(param);

            param = new ElementParameter(process);
            param.setName(EParameterName.UPDATE_COMPONENTS.getName());
            param.setValue(Boolean.FALSE);
            param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
            param.setField(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(1);
            param.setReadOnly(true);
            param.setRequired(false);
            param.setShow(false);
            paramList.add(param);
            // zywang added to fix feature 5545
            param = new ElementParameter(process);
            param.setName(EParameterName.ICONSELECTION.getName());
            param.setValue(""); //$NON-NLS-1$
            param.setDisplayName(EParameterName.ICONSELECTION.getDisplayName());
            param.setField(EParameterFieldType.ICON_SELECTION);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(3);
            param.setReadOnly(false);
            param.setRequired(false);
            paramList.add(param);
        }

        param = new ElementParameter(process);
        param.setName(EParameterName.IMPLICIT_TCONTEXTLOAD.getName());
        param.setValue(false);
        param.setGroupDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName()); //$NON-NLS-1$
        param.setDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD.getDisplayName());
        param.setField(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(3);
        param.setShow(!isJoblet);
        paramList.add(param);

        // on files
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName()));
        param.setValue(false);
        param.setDisplayName(EParameterName.FROM_FILE_FLAG.getDisplayName());
        param.setField(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(4);
        param.setShowIf(JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION));
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // on database
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName()));
        param.setValue(false);
        param.setDisplayName(EParameterName.FROM_DATABASE_FLAG.getDisplayName());
        param.setField(EParameterFieldType.RADIO);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(5);
        param.setShowIf(JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION));
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // on file
        createExtraOnFileParameters(process);
        // on database
        createExtraOnDBParameters(process);
        // tContextLoad
        createExtraContextLoadParameters(process);

    }

    private static void createExtraOnFileParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) process.getElementParameters();
        // set Implicit tContextLoad file
        String fileName = ElementParameterParser.parse(process, "__COMP_DEFAULT_FILE_DIR__/in.csv"); //$NON-NLS-1$
        IPath path = Path.fromOSString(fileName);
        fileName = TalendTextUtils.addQuotes(path.toPortableString());

        param = new ElementParameter(process);
        param.setName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName());
        param.setValue(fileName);
        param.setDisplayName(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getDisplayName());
        param.setField(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(31);
        String condition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " //$NON-NLS-1$ 
                + JobSettingsConstants.addBrackets(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG
                        .getName())
                        + " == 'true'"); //$NON-NLS-1$

        param.setShowIf(condition);
        paramList.add(param);

        param = new ElementParameter(process);
        param.setName(EParameterName.FIELDSEPARATOR.getName());

        String value = ";"; //$NON-NLS-1$
        value = TalendTextUtils.addQuotes(value);

        param.setValue(value);
        param.setDisplayName(EParameterName.FIELDSEPARATOR.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setGroup(IMPLICIT_GROUP);
        param.setNumRow(32);
        condition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " //$NON-NLS-1$ 
                + JobSettingsConstants.addBrackets(JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG
                        .getName())
                        + " == 'true'"); //$NON-NLS-1$

        param.setShowIf(condition);
        paramList.add(param);

    }

    private static void createExtraOnDBParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) process.getElementParameters();

        // checks current language, if it is perl, set languageType to 0(default value), otherwise to 1.
        int languageType = 0;
        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            languageType = 1;
        }
        final String onDBCondition = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName())
                + " == 'true'"; //$NON-NLS-1$
        final String dbCondition = JobSettingsConstants.addBrackets(CONTEXTLOAD_CONDITION)
                + " and " + JobSettingsConstants.addBrackets(onDBCondition); //$NON-NLS-1$

        // property type
        ElementParameter parentPropertyType = new ElementParameter(process);
        parentPropertyType.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()));
        parentPropertyType.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        parentPropertyType.setValue(""); //$NON-NLS-1$
        parentPropertyType.setCategory(EComponentCategory.EXTRA);
        parentPropertyType.setField(EParameterFieldType.PROPERTY_TYPE);
        parentPropertyType.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        parentPropertyType.setNumRow(41);
        parentPropertyType.setShowIf(dbCondition);
        parentPropertyType.setGroup(IMPLICIT_GROUP);
        paramList.add(parentPropertyType);

        param = new ElementParameter(process);
        param.setName(EParameterName.PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        param.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        param.setValue(EmfComponent.BUILTIN);
        param.setCategory(EComponentCategory.EXTRA);
        param.setField(EParameterFieldType.TECHNICAL);
        param.setRepositoryValue(ERepositoryCategoryType.DATABASE.getName());
        param.setNumRow(41);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        // repository property type
        param = new ElementParameter(process);
        param.setName(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        param.setDisplayName(EParameterName.REPOSITORY_PROPERTY_TYPE.getDisplayName());
        param.setListItemsDisplayName(new String[] {});
        param.setListItemsValue(new String[] {});
        // param.setValue(""); //$NON-NLS-1$
        param.setCategory(EComponentCategory.EXTRA);
        param.setField(EParameterFieldType.TECHNICAL);
        param.setShow(false);
        param.setRequired(true);
        param.setNumRow(41);
        param.setGroup(IMPLICIT_GROUP);
        param.setParentParameter(parentPropertyType);
        // paramList.add(param);

        // dbType
        final String dbTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName());
        param = new ElementParameter(process);
        param.setName(dbTypeName);
        param.setDisplayName(EParameterName.DB_TYPE.getDisplayName());
        param.setField(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.EXTRA);
        param.setListItemsDisplayName(StatsAndLogsConstants.DISPLAY_DBNAMES[languageType]);
        param.setListItemsValue(JobSettingsConstants.DB_INPUT_COMPONENTS[languageType]);
        param.setListRepositoryItems(StatsAndLogsConstants.REPOSITORY_ITEMS[languageType]);
        param.setListItemsDisplayCodeName(StatsAndLogsConstants.CODE_LIST[languageType]);
        // param.setValue(JobSettingsConstants.DB_INPUT_COMPONENTS[languageType][0]);
        param.setNumRow(42);
        param.setRepositoryValue("TYPE"); //$NON-NLS-1$
        param.setRequired(true);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // dbVersion
        final String dbVersionName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
        if (process.getElementParameter(dbVersionName) == null) {
            param = new ElementParameter(process);
            param.setName(dbVersionName);
            param.setValue(StatsAndLogsConstants.DB_VERSION_DRIVER[1]);
            param.setDisplayName(EParameterName.DB_VERSION.getDisplayName());
            param.setField(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.EXTRA);
            param.setListItemsDisplayName(StatsAndLogsConstants.DB_VERSION_DISPLAY);
            param.setListItemsValue(StatsAndLogsConstants.DB_VERSION_DRIVER);
            param.setListItemsDisplayCodeName(StatsAndLogsConstants.DB_VERSION_CODE);
            param.setNumRow(42);
            param.setRepositoryValue("DB_VERSION"); //$NON-NLS-1$
            param.setRequired(true);
            param
                    .setShowIf(dbCondition
                            + " and (" + dbTypeName + " == 'OCLE' or " + dbTypeName + " == 'OCLE_OCI' or " + dbTypeName + " =='ACCESS' or " + dbTypeName + " =='MYSQL') "); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$   
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }

        // host
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.HOST.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(43);
        param.setRepositoryValue("SERVER_NAME"); //$NON-NLS-1$
        String dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + "!='OCLE_OCI'"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$ 
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // port
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.PORT.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(43);
        param.setRepositoryValue("PORT"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + " != 'FIREBIRD'" + " and " + dbTypeName + "!='OCLE_OCI'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$ 
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // dbName
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.DBNAME.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        dbCon = dbTypeName
                + " != 'SQLITE'" + " and " + dbTypeName + " != 'ACCESS'" + " and " + dbTypeName + " != 'FIREBIRD'" + " and " + dbTypeName + "!='OCLE_OCI'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$ 
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // local service name
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.LOCAL_SERVICE_NAME.getName()));
        param.setValue(StatsAndLogsManager.addQuotes(""));
        param.setDisplayName(EParameterName.LOCAL_SERVICE_NAME.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("SID"); //$NON-NLS-1$
        dbCon = dbTypeName + " =='OCLE_OCI' ";
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$ 
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {

            // additional parameters
            param = new ElementParameter(process);
            param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName()));
            param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
            param.setDisplayName(EParameterName.PROPERTIES.getDisplayName());
            param.setField(EParameterFieldType.TEXT);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(44);
            param.setRepositoryValue("PROPERTIES_STRING"); //$NON-NLS-1$
            dbCon = dbTypeName
                    + " == 'SQL_SERVER'" + " or " + dbTypeName + " == 'MYSQL'" + " or " + dbTypeName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + " == 'INFORMIX'" + " or " + dbTypeName + " == 'OCLE'" + " or " + dbTypeName + " == 'OCLE_OCI'" + " or " + dbTypeName + " == 'SYBASE'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

            param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$ 
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }
        // schema
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.SCHEMA_DB.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(44);
        param.setRepositoryValue("SCHEMA"); //$NON-NLS-1$
        final String schemaCondition = JobSettingsConstants
                .addBrackets(dbTypeName
                        + " =='OCLE' or " + dbTypeName //$NON-NLS-1$
                        + " =='POSTGRESQL' or " + dbTypeName + " =='POSTGRESPLUS' or " + dbTypeName + " =='OCLE_OCI' or " + dbTypeName + " =='SQL_SERVER' or " + dbTypeName + " =='INFORMIX'"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
        param.setShowIf(schemaCondition + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // username
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.USER.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(45);
        param.setRequired(true);
        param.setRepositoryValue("USERNAME"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // password
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.PASS.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(45);
        param.setRequired(true);
        param.setRepositoryValue("PASSWORD"); //$NON-NLS-1$
        dbCon = dbTypeName + " != 'SQLITE'"; //$NON-NLS-1$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);
        // databse file path
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBFILE.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.DBFILE.getDisplayName());
        param.setField(EParameterFieldType.FILE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(46);
        param.setRepositoryValue("FILE"); //$NON-NLS-1$
        dbCon = dbTypeName + " == 'SQLITE'" + " or " + dbTypeName + " == 'ACCESS'" + " or " + dbTypeName + " == 'FIREBIRD'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        param.setShowIf(JobSettingsConstants.addBrackets(dbCon) + " and " + dbCondition); //$NON-NLS-1$ 
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // table
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName()));
        param.setValue(StatsAndLogsManager.addQuotes("")); //$NON-NLS-1$
        param.setDisplayName(EParameterName.DBTABLE.getDisplayName());
        param.setField(EParameterFieldType.DBTABLE);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(47);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        // query condition
        param = new ElementParameter(process);
        param.setName(JobSettingsConstants.getExtraParameterName(EParameterName.QUERY_CONDITION.getName()));
        param.setValue(QUOTE + QUOTE);
        param.setDisplayName(EParameterName.QUERY_CONDITION.getDisplayName());
        param.setField(EParameterFieldType.TEXT);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(48);
        param.setShowIf(dbCondition);
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

    }

    private static void createExtraContextLoadParameters(IProcess process) {
        ElementParameter param;
        List<IElementParameter> paramList = (List<IElementParameter>) process.getElementParameters();

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            final String[] itemValues = new String[] { ContextLoadInfo.ERROR.getDisplayName(),
                    ContextLoadInfo.WARNING.getDisplayName(), ContextLoadInfo.INFO.getDisplayName() };

            //
            param = new ElementParameter(process);
            param.setName(EParameterName.LOAD_NEW_VARIABLE.getName());
            param.setDisplayName(EParameterName.LOAD_NEW_VARIABLE.getDisplayName());
            param.setValue(ContextLoadInfo.WARNING.getDisplayName());
            param.setListItemsDisplayName(itemValues);
            param.setListItemsValue(itemValues);
            param.setField(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(80);
            param.setShowIf(CONTEXTLOAD_CONDITION);
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
            //
            param = new ElementParameter(process);
            param.setName(EParameterName.NOT_LOAD_OLD_VARIABLE.getName());
            param.setDisplayName(EParameterName.NOT_LOAD_OLD_VARIABLE.getDisplayName());
            param.setValue(ContextLoadInfo.WARNING.getDisplayName());
            param.setListItemsDisplayName(itemValues);
            param.setListItemsValue(itemValues);
            param.setField(EParameterFieldType.CLOSED_LIST);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(81);
            param.setShowIf(CONTEXTLOAD_CONDITION);
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }
        // print operations
        param = new ElementParameter(process);
        param.setName(EParameterName.PRINT_OPERATIONS.getName());
        param.setValue(false);
        param.setDisplayName(EParameterName.PRINT_OPERATIONS.getDisplayName());
        param.setField(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(82);
        param.setRequired(true);
        param.setShowIf("((PRINT_OPERATIONS == 'true' or PRINT_OPERATIONS == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            // disable error
            param = new ElementParameter(process);
            param.setName(EParameterName.DISABLE_ERROR.getName());
            param.setValue(false);
            param.setDisplayName(EParameterName.DISABLE_ERROR.getDisplayName());
            param.setField(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(83);
            param.setRequired(true);
            param.setShowIf("((DISABLE_ERROR == 'true' or DISABLE_ERROR == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }

        // disable warnings
        param = new ElementParameter(process);
        param.setName(EParameterName.DISABLE_WARNINGS.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.DISABLE_WARNINGS.getDisplayName());
        param.setField(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.EXTRA);
        param.setNumRow(83);
        param.setRequired(true);
        param.setShowIf("((DISABLE_WARNINGS == 'true' or DISABLE_WARNINGS == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        param.setGroup(IMPLICIT_GROUP);
        paramList.add(param);

        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            // disable info
            param = new ElementParameter(process);
            param.setName(EParameterName.DISABLE_INFO.getName());
            param.setValue(true);
            param.setDisplayName(EParameterName.DISABLE_INFO.getDisplayName());
            param.setField(EParameterFieldType.CHECK);
            param.setCategory(EComponentCategory.EXTRA);
            param.setNumRow(83);
            param.setRequired(true);
            param.setShowIf("((DISABLE_INFO == 'true' or DISABLE_INFO == 'false') and " + CONTEXTLOAD_CONDITION + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            param.setGroup(IMPLICIT_GROUP);
            paramList.add(param);
        }

    }

    /**
     * 
     * create parameter for context tab.
     */
    private static void createContextParameters(IProcess process) {
        //
    }

    /**
     * 
     * DOC ggu Comment method "isStatsAndLogsActivated".
     * 
     * for stats & logs settings
     */
    public static boolean isStatsAndLogsActivated(IProcess process) {
        return StatsAndLogsManager.isStatsAndLogsActivated(process);
    }

    public static List<DataNode> createStatsAndLogsNodes(Process process) {
        return StatsAndLogsManager.getStatsAndLogsNodes(process);
    }

    /**
     * 
     * DOC ggu Comment method "isImplicittContextLoadActived".
     * 
     * for implictit tContextLoad in extra settings
     */
    public static boolean isImplicittContextLoadActived(IProcess process) {
        String paramName = EParameterName.IMPLICIT_TCONTEXTLOAD.getName();
        boolean useContextLoad = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!useContextLoad) {
            // not used
            return false;
        }

        // file
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName());
        boolean fileFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());

        // db
        String dbInput = null;
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName());
        boolean dbFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!dbFlag) {
            dbInput = null;
        } else {
            dbInput = (String) process.getElementParameter(
                    JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName())).getValue();
            if (dbInput == null || dbInput.equals("")) { //$NON-NLS-1$
                dbInput = null;
                dbFlag = false;
            }
        }

        if (!fileFlag && !dbFlag) {
            // not used
            return false;
        }

        return true;
    }

    public static List<DataNode> createExtraContextLoadNodes(IProcess process) {
        List<DataNode> nodeList = new ArrayList<DataNode>();

        String paramName = EParameterName.IMPLICIT_TCONTEXTLOAD.getName();
        boolean useContextLoad = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!useContextLoad) {
            // not used
            return Collections.emptyList();
        }

        // file
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_FILE_FLAG.getName());
        boolean fileFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());

        // db
        String dbInput = null;
        paramName = JobSettingsConstants.getExtraParameterName(EParameterName.FROM_DATABASE_FLAG.getName());
        boolean dbFlag = ((Boolean) process.getElementParameter(paramName).getValue())
                && process.getElementParameter(paramName).isShow(process.getElementParameters());
        if (!dbFlag) {
            dbInput = null;
        } else {

            dbInput = (String) process.getElementParameter(
                    JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName())).getValue();
            dbInput = OracleComponentHelper.filterOracleComponentName(dbInput);
            if (dbInput == null || dbInput.equals("")) { //$NON-NLS-1$
                dbInput = null;
                dbFlag = false;
            }
        }

        if (!fileFlag && !dbFlag) {
            // not used
            return Collections.emptyList();
        }

        IComponent tContextLoadComponent = new JobContextLoadComponent(fileFlag, dbInput);

        final String uniqueName = "Implicit_Context"; //$NON-NLS-1$
        DataNode tContextLoadNode = new DataNode(tContextLoadComponent, uniqueName);
        tContextLoadNode.setStart(true);
        tContextLoadNode.setSubProcessStart(true);
        tContextLoadNode.setActivate(true);

        IMetadataTable table = getSchemaTablefromComponent(JobContextLoadComponent.CONTEXTLOAD_COMPONENT, uniqueName);
        if (table != null) {
            tContextLoadNode.getMetadataList().clear();
            tContextLoadNode.getMetadataList().add(table);

        }
        // set parameters
        IElementParameter param = null;
        if (fileFlag) {
            // is file
            String inputFile = (String) process.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName())
                    .getValue();
            String fileSparator = (String) process.getElementParameter(EParameterName.FIELDSEPARATOR.getName()).getValue();
            tContextLoadNode.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName()).setValue(inputFile);
            tContextLoadNode.getElementParameter(EParameterName.FIELDSEPARATOR.getName()).setValue(fileSparator);
        } else {
            // is db
            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.HOST.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.PORT.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DBNAME.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_VERSION.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTIES.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.SCHEMA_DB.getName());
            param = process.getElementParameter(paramName);
            if (param != null) {
                tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
            }
            String schema = (String) process.getElementParameter(paramName).getValue();
            if (schema != null) {
                schema = TalendTextUtils.removeQuotes(schema);
            }

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.USER.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.PASS.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.CONNECTION_TYPE.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(
                    OracleComponentHelper.filterOracleConnectionType((String) process.getElementParameter(
                            JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName())).getValue()));

            paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DBTABLE.getName());
            tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

            // query
            String dbTableName = (String) process.getElementParameter(paramName).getValue();
            String realTableName = getCurrentTableName(dbTableName);
            if (realTableName == null) {
                realTableName = QueryUtil.DEFAULT_TABLE_NAME;
            }

            String dbType = getDatabaseTypeFromParameter(process);
            if (dbType != null) {
                String query = TalendTextUtils.addSQLQuotes(QueryUtil
                        .generateNewQuery(null, table, dbType, schema, realTableName));
                paramName = JobSettingsConstants.getExtraParameterName(EParameterName.QUERY_CONDITION.getName());
                String conditionStatement = (String) process.getElementParameter(paramName).getValue();
                if (conditionStatement != null) {
                    String tmp = TalendTextUtils.removeQuotes(conditionStatement);
                    if (!"".equals(tmp)) { //$NON-NLS-1$
                        query = query + CONNECTOR + QUOTE + " WHERE " + QUOTE + CONNECTOR + conditionStatement; //$NON-NLS-1$
                    }
                }
                final String quoteByDBType = TalendTextUtils.getQuoteByDBType(dbType, false);
                EDatabaseTypeName dbTypeName = EDatabaseTypeName.getTypeFromDbType(dbType);
                if (dbTypeName == EDatabaseTypeName.MSSQL) {
                    query = query.replaceAll("(?i)\\bkey\\b", //$NON-NLS-1$ 
                            "\\\\" + quoteByDBType + "key\\\\" + quoteByDBType); //$NON-NLS-1$  //$NON-NLS-2$ 
                }
                tContextLoadNode.getElementParameter(JobSettingsConstants.QUERY).setValue(query);
            }
        }
        // tContextLoad
        paramName = EParameterName.LOAD_NEW_VARIABLE.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.NOT_LOAD_OLD_VARIABLE.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.PRINT_OPERATIONS.getName();
        tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

        paramName = EParameterName.DISABLE_ERROR.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.DISABLE_INFO.getName();
        param = process.getElementParameter(paramName);
        if (param != null) {
            tContextLoadNode.getElementParameter(paramName).setValue(param.getValue());
        }

        paramName = EParameterName.DISABLE_WARNINGS.getName();
        tContextLoadNode.getElementParameter(paramName).setValue(process.getElementParameter(paramName).getValue());

        tContextLoadNode.setProcess(process);
        nodeList.add(tContextLoadNode);

        return nodeList;
    }

    private static IMetadataTable getSchemaTablefromComponent(final String componentName, final String tableName) {
        IComponent tmpComponent = ComponentsFactoryProvider.getInstance().get(componentName);

        DataNode tmpNode = new DataNode(tmpComponent, "tmp"); //$NON-NLS-1$
        for (int k = 0; k < tmpNode.getElementParameters().size(); k++) {
            IElementParameter currentParam = tmpNode.getElementParameters().get(k);
            if (currentParam.getField().equals(EParameterFieldType.SCHEMA_TYPE)) {
                Object value = currentParam.getValue();
                if (value instanceof IMetadataTable) {
                    IMetadataTable table = null;
                    if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
                        table = ((IMetadataTable) value).clone(true);
                    } else {
                        table = ((IMetadataTable) value).clone();
                    }
                    table.setTableName(tableName);
                    table.setAttachedConnector(currentParam.getContext());
                    return table;
                }

            }
        }
        return null;
    }

    private static String getCurrentTableName(String dbTableName) {
        String currentTableName = null;
        if (dbTableName == null) {
            dbTableName = QueryUtil.DEFAULT_TABLE_NAME;
        } else {
            switch (LanguageManager.getCurrentLanguage()) {
            case JAVA:
                if (dbTableName.contains(TalendTextUtils.QUOTATION_MARK)) {
                    if (dbTableName.startsWith(TalendTextUtils.QUOTATION_MARK)
                            && dbTableName.endsWith(TalendTextUtils.QUOTATION_MARK) && dbTableName.length() > 2) {
                        currentTableName = dbTableName.substring(1, dbTableName.length() - 1);
                    } else {
                        currentTableName = null;
                    }
                } else {
                    currentTableName = dbTableName;
                }
                break;
            default:
                if (dbTableName.contains(TalendTextUtils.SINGLE_QUOTE)) {
                    if (dbTableName.startsWith(TalendTextUtils.SINGLE_QUOTE)
                            && dbTableName.endsWith(TalendTextUtils.SINGLE_QUOTE) && dbTableName.length() > 2) {
                        currentTableName = dbTableName.substring(1, dbTableName.length() - 1);
                    } else {
                        currentTableName = null;
                    }
                } else {
                    currentTableName = dbTableName;
                }
            }
        }
        return currentTableName;
    }

    private static String getDatabaseTypeFromParameter(IProcess process) {
        String paramName = JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName());
        IElementParameter param = process.getElementParameter(paramName);
        String value = (String) param.getValue();

        if (value != null && param.getName().equals(JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName()))) {
            for (int i = 0; i < param.getListItemsValue().length; i++) {
                Object obj = param.getListItemsValue()[i];
                String itemValue = (String) obj;
                if (itemValue.equals(value)) {
                    return param.getListItemsDisplayName()[i];
                }
            }
        }
        return null;
    }
}
