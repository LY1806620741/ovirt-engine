package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetQuotaByQuotaIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetQuotaByStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class QuotaListModel extends ListWithDetailsModel implements ISupportSystemTreeContext {

    private UICommand createQuotaCommand;
    private UICommand removeQuotaCommand;
    private UICommand editQuotaCommand;

    public UICommand getCreateQuotaCommand() {
        return createQuotaCommand;
    }

    public void setCreateQuotaCommand(UICommand createQuotaCommand) {
        this.createQuotaCommand = createQuotaCommand;
    }

    public UICommand getRemoveQuotaCommand() {
        return removeQuotaCommand;
    }

    public void setRemoveQuotaCommand(UICommand removeQuotaCommand) {
        this.removeQuotaCommand = removeQuotaCommand;
    }

    public UICommand getEditQuotaCommand() {
        return editQuotaCommand;
    }

    public void setEditQuotaCommand(UICommand editQuotaCommand) {
        this.editQuotaCommand = editQuotaCommand;
    }

    public QuotaListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().quotaTitle());

        setDefaultSearchString("Quota:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());

        setCreateQuotaCommand(new UICommand("Create", this)); //$NON-NLS-1$
        setEditQuotaCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveQuotaCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    @Override
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        updateActionAvailability();
    }

    @Override
    protected void OnSelectedItemChanged() {
        super.OnSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void InitDetailModels() {
        super.InitDetailModels();
        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new QuotaClusterListModel());
        list.add(new QuotaStorageListModel());
        list.add(new QuotaVmListModel());
        list.add(new QuotaTemplateListModel());
        list.add(new QuotaUserListModel());
        list.add(new QuotaPermissionListModel());
        list.add(new QuotaEventListModel());

        setDetailModels(list);
    }

    @Override
    protected void SyncSearch() {
        super.SyncSearch();
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.Model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                QuotaListModel quotaListModel = (QuotaListModel) model;
                quotaListModel.setItems((ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue());

            }
        };
        GetQuotaByStoragePoolIdQueryParameters parameters = new GetQuotaByStoragePoolIdQueryParameters();
        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType().equals(SystemTreeItemType.DataCenter)) {
            parameters.setStoragePoolId(((storage_pool) getSystemTreeSelectedItem().getEntity()).getId());
        }

        parameters.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetQuotaByStoragePoolId,
                parameters,
                asyncQuery);
    }

    private void updateActionAvailability() {
        boolean isOnlyOneItemSelected =
                (getSelectedItems() == null && getSelectedItem() != null)
                        || (getSelectedItems() != null && getSelectedItems().size() == 1);
        getEditQuotaCommand().setIsExecutionAllowed(isOnlyOneItemSelected);
        getRemoveQuotaCommand().setIsExecutionAllowed(isOnlyOneItemSelected);
    }

    private void createQuota() {
        final QuotaModel qModel = new QuotaModel();
        qModel.setTitle(ConstantsManager.getInstance().getConstants().newQuotaTitle());
        qModel.setHashName("new_quota"); //$NON-NLS-1$
        Quota newQuota = new Quota();
        qModel.setEntity(newQuota);
        qModel.getGraceCluster().setEntity(newQuota.getGraceVdsGroupPercentage());
        qModel.getThresholdCluster().setEntity(newQuota.getThresholdVdsGroupPercentage());
        qModel.getGraceStorage().setEntity(newQuota.getGraceStoragePercentage());
        qModel.getThresholdStorage().setEntity(newQuota.getThresholdStoragePercentage());
        setWindow(qModel);

        AsyncDataProvider.GetDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ArrayList<storage_pool> dataCenterList = (ArrayList<storage_pool>) returnValue;
                if (dataCenterList == null || dataCenterList.size() == 0) {
                    return;
                }
                QuotaListModel quotaListModel = (QuotaListModel) model;
                QuotaModel quotaModel = (QuotaModel) quotaListModel.getWindow();
                quotaModel.getDataCenter().setItems(dataCenterList);
                quotaModel.getDataCenter().setSelectedItem(dataCenterList.get(0));
            }
        }));

        qModel.getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                storage_pool selectedDataCenter = (storage_pool) qModel.getDataCenter().getSelectedItem();
                AsyncDataProvider.GetClusterList(new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) returnValue;
                        if (clusterList == null || clusterList.size() == 0) {
                            qModel.getAllDataCenterClusters().setItems(new ArrayList<QuotaVdsGroup>());
                            return;
                        }
                        ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
                        QuotaVdsGroup quotaVdsGroup;
                        for (VDSGroup vdsGroup : clusterList) {
                            quotaVdsGroup = new QuotaVdsGroup();
                            quotaVdsGroup.setVdsGroupId(vdsGroup.getId());
                            quotaVdsGroup.setVdsGroupName(vdsGroup.getname());
                            quotaVdsGroup.setMemSizeMBUsage((long) 0);
                            quotaVdsGroup.setVirtualCpuUsage(0);
                            quotaClusterList.add(quotaVdsGroup);
                        }
                        qModel.getAllDataCenterClusters().setItems(quotaClusterList);

                    }
                }), selectedDataCenter.getId());
                AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<storage_domains> storageList = (ArrayList<storage_domains>) returnValue;
                        if (storageList == null || storageList.size() == 0) {
                            qModel.getAllDataCenterStorages().setItems(new ArrayList<QuotaStorage>());
                            return;
                        }
                        ArrayList<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
                        QuotaStorage quotaStorage;
                        for (storage_domains storage : storageList) {
                            if (!storage.getstorage_domain_type().equals(StorageDomainType.Master)
                                    && !storage.getstorage_domain_type().equals(StorageDomainType.Data)) {
                                continue;
                            }
                            quotaStorage = new QuotaStorage();
                            quotaStorage.setStorageId(storage.getId());
                            quotaStorage.setStorageName(storage.getstorage_name());
                            quotaStorage.setStorageSizeGBUsage((double) 0);
                            quotaStorageList.add(quotaStorage);
                        }
                        qModel.getAllDataCenterStorages().setItems(quotaStorageList);
                    }
                }), selectedDataCenter.getId());

            }
        });

        UICommand command = new UICommand("OnCreateQuota", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        qModel.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        qModel.getCommands().add(command);
    }

    private void cancel() {
        setWindow(null);
        setConfirmWindow(null);
    }

    private void onCreateQuota() {
        QuotaModel model = (QuotaModel) getWindow();
        if (!model.Validate()) {
            return;
        }
        Quota quota = (Quota) model.getEntity();
        quota.setQuotaName((String) model.getName().getEntity());
        quota.setDescription((String) model.getDescription().getEntity());
        quota.setStoragePoolId(((storage_pool) model.getDataCenter().getSelectedItem()).getId());

        quota.setGraceVdsGroupPercentage(model.getGraceClusterAsInteger());
        quota.setGraceStoragePercentage(model.getGraceStorageAsInteger());
        quota.setThresholdVdsGroupPercentage(model.getThresholdClusterAsInteger());
        quota.setThresholdStoragePercentage(model.getThresholdStorageAsInteger());

        if ((Boolean) model.getGlobalClusterQuota().getEntity()) {
            QuotaVdsGroup quotaVdsGroup;
            for (QuotaVdsGroup iter : (ArrayList<QuotaVdsGroup>) model.getQuotaClusters().getItems()) {
                quota.setGlobalQuotaVdsGroup(new QuotaVdsGroup());
                quota.getGlobalQuotaVdsGroup().setMemSizeMB(iter.getMemSizeMB());
                quota.getGlobalQuotaVdsGroup().setVirtualCpu(iter.getVirtualCpu());
                quota.getQuotaVdsGroups().clear();
            }
        } else {
            quota.setGlobalQuotaVdsGroup(null);
            ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
            QuotaVdsGroup quotaVdsGroup;
            for (QuotaVdsGroup iter : (ArrayList<QuotaVdsGroup>) model.getAllDataCenterClusters().getItems()) {
                quotaVdsGroup = iter;
                if (quotaVdsGroup.getMemSizeMB() != null) {
                    quotaClusterList.add(quotaVdsGroup);
                }
            }
            quota.setQuotaVdsGroups(quotaClusterList);
        }

        if ((Boolean) model.getGlobalStorageQuota().getEntity()) {
            QuotaStorage quotaStorage;
            for (QuotaStorage iter : (ArrayList<QuotaStorage>) model.getQuotaStorages().getItems()) {
                quota.setGlobalQuotaStorage(new QuotaStorage());
                quota.getGlobalQuotaStorage().setStorageSizeGB(iter.getStorageSizeGB());
                quota.getQuotaStorages().clear();
            }
        } else {
            quota.setGlobalQuotaStorage(null);
            ArrayList<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
            QuotaStorage quotaStorage;
            for (QuotaStorage iter : (ArrayList<QuotaStorage>) model.getAllDataCenterStorages().getItems()) {
                quotaStorage = iter;
                if (quotaStorage.getStorageSizeGB() != null) {
                    quotaStorageList.add(quotaStorage);
                }
            }
            quota.setQuotaStorages(quotaStorageList);
        }

        VdcActionType actionType = VdcActionType.AddQuota;
        if (!quota.getId().equals(Guid.Empty)) {
            actionType = VdcActionType.UpdateQuota;
        }
        Frontend.RunAction(actionType,
                new QuotaCRUDParameters(quota),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void Executed(FrontendActionAsyncResult result) {
                        setWindow(null);
                    }
                });
    }

    private void editQuota() {
        Quota outer_quota = (Quota) getSelectedItem();
        QuotaModel qModel = new QuotaModel();
        qModel.getName().setEntity(outer_quota.getQuotaName());

        qModel.getGraceCluster().setEntity(outer_quota.getGraceVdsGroupPercentage());
        qModel.getThresholdCluster().setEntity(outer_quota.getThresholdVdsGroupPercentage());
        qModel.getGraceStorage().setEntity(outer_quota.getGraceStoragePercentage());
        qModel.getThresholdStorage().setEntity(outer_quota.getThresholdStoragePercentage());

        qModel.getDescription().setEntity(outer_quota.getDescription());
        qModel.setTitle(ConstantsManager.getInstance().getConstants().editQuotaTitle());
        qModel.setHashName("edit_quota"); //$NON-NLS-1$
        UICommand command = new UICommand("OnCreateQuota", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        qModel.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        qModel.getCommands().add(command);
        setWindow(qModel);
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.Model = this;
        asyncQuery.asyncCallback = new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                QuotaListModel outer_quotaListModel = (QuotaListModel) model;
                final QuotaModel quotaModel = (QuotaModel) outer_quotaListModel.getWindow();
                final Quota quota = (Quota) ((VdcQueryReturnValue) returnValue).getReturnValue();
                quotaModel.setEntity(quota);
                boolean hasSpecificLimitation =
                        quota.getQuotaVdsGroups() == null || quota.getQuotaVdsGroups().size() == 0;
                if (hasSpecificLimitation
                        && quota.getGlobalQuotaVdsGroup() != null) {
                    quotaModel.getGlobalClusterQuota().setEntity(true);
                    QuotaVdsGroup cluster =
                            ((ArrayList<QuotaVdsGroup>) quotaModel.getQuotaClusters().getItems()).get(0);
                    cluster.setMemSizeMB(quota.getGlobalQuotaVdsGroup().getMemSizeMB());
                    cluster.setVirtualCpu(quota.getGlobalQuotaVdsGroup().getVirtualCpu());
                    cluster.setMemSizeMBUsage(quota.getGlobalQuotaVdsGroup().getMemSizeMBUsage());
                    cluster.setVirtualCpuUsage(quota.getGlobalQuotaVdsGroup().getVirtualCpuUsage());
                } else {
                    quotaModel.getSpecificClusterQuota().setEntity(true);
                }
                hasSpecificLimitation = quota.getQuotaStorages() == null || quota.getQuotaStorages().size() == 0;
                if (hasSpecificLimitation
                        && quota.getGlobalQuotaStorage() != null) {
                    quotaModel.getGlobalStorageQuota().setEntity(true);
                    QuotaStorage storage = ((ArrayList<QuotaStorage>) quotaModel.getQuotaStorages().getItems()).get(0);
                    storage.setStorageSizeGB(quota.getGlobalQuotaStorage().getStorageSizeGB());
                    storage.setStorageSizeGBUsage(quota.getGlobalQuotaStorage().getStorageSizeGBUsage());
                } else {
                    quotaModel.getSpecificStorageQuota().setEntity(true);
                }
                AsyncDataProvider.GetDataCenterList(new AsyncQuery(outer_quotaListModel, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<storage_pool> dataCenterList = (ArrayList<storage_pool>) returnValue;
                        if (dataCenterList == null || dataCenterList.size() == 0) {
                            return;
                        }
                        quotaModel.getDataCenter().setItems(dataCenterList);
                        for (storage_pool storage_pool : dataCenterList) {
                            if (storage_pool.getId().equals(quota.getStoragePoolId())) {
                                quotaModel.getDataCenter().setSelectedItem(storage_pool);
                                break;
                            }
                        }
                    }
                }));

                quotaModel.getDataCenter().getSelectedItemChangedEvent().addListener(new IEventListener() {

                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        storage_pool selectedDataCenter = (storage_pool) quotaModel.getDataCenter().getSelectedItem();
                        AsyncDataProvider.GetClusterList(new AsyncQuery(this, new INewAsyncCallback() {

                            @Override
                            public void OnSuccess(Object model, Object returnValue) {
                                ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) returnValue;
                                if (clusterList == null || clusterList.size() == 0) {
                                    quotaModel.getAllDataCenterClusters().setItems(new ArrayList<QuotaVdsGroup>());
                                    return;
                                }
                                ArrayList<QuotaVdsGroup> quotaClusterList = new ArrayList<QuotaVdsGroup>();
                                QuotaVdsGroup quotaVdsGroup;
                                for (VDSGroup vdsGroup : clusterList) {
                                    quotaVdsGroup = new QuotaVdsGroup();
                                    quotaVdsGroup.setVdsGroupId(vdsGroup.getId());
                                    quotaVdsGroup.setVdsGroupName(vdsGroup.getname());
                                    quotaVdsGroup.setQuotaId(quota.getId());
                                    boolean containCluster = false;
                                    for (QuotaVdsGroup iter : quota.getQuotaVdsGroups()) {
                                        if (quotaVdsGroup.getVdsGroupId().equals(iter.getVdsGroupId())) {
                                            quotaVdsGroup.setQuotaVdsGroupId(iter.getQuotaVdsGroupId());
                                            quotaVdsGroup.setMemSizeMB(iter.getMemSizeMB());
                                            quotaVdsGroup.setVirtualCpu(iter.getVirtualCpu());
                                            quotaVdsGroup.setMemSizeMBUsage(iter.getMemSizeMBUsage());
                                            quotaVdsGroup.setVirtualCpuUsage(iter.getVirtualCpuUsage());
                                            containCluster = true;
                                            break;
                                        }
                                    }
                                    if (!containCluster) {
                                        quotaVdsGroup.setMemSizeMBUsage((long) 0);
                                        quotaVdsGroup.setVirtualCpuUsage(0);
                                    }
                                    quotaClusterList.add(quotaVdsGroup);
                                }
                                quotaModel.getAllDataCenterClusters().setItems(quotaClusterList);

                            }
                        }), selectedDataCenter.getId());
                        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {

                            @Override
                            public void OnSuccess(Object model, Object returnValue) {
                                ArrayList<storage_domains> storageList = (ArrayList<storage_domains>) returnValue;

                                if (storageList == null || storageList.size() == 0) {
                                    quotaModel.getAllDataCenterStorages().setItems(new ArrayList<QuotaStorage>());
                                    return;
                                }
                                ArrayList<QuotaStorage> quotaStorageList = new ArrayList<QuotaStorage>();
                                QuotaStorage quotaStorage;
                                for (storage_domains storage : storageList) {
                                    if (!storage.getstorage_domain_type().equals(StorageDomainType.Master)
                                            && !storage.getstorage_domain_type().equals(StorageDomainType.Data)) {
                                        continue;
                                    }
                                    quotaStorage = new QuotaStorage();
                                    quotaStorage.setStorageId(storage.getId());
                                    quotaStorage.setStorageName(storage.getstorage_name());
                                    quotaStorage.setQuotaId(quota.getId());
                                    boolean containStorage = false;
                                    for (QuotaStorage iter : quota.getQuotaStorages()) {
                                        if (quotaStorage.getStorageId().equals(iter.getStorageId())) {
                                            quotaStorage.setQuotaStorageId(iter.getQuotaStorageId());
                                            quotaStorage.setStorageSizeGB(iter.getStorageSizeGB());
                                            quotaStorage.setStorageSizeGBUsage(iter.getStorageSizeGBUsage());
                                            containStorage = true;
                                            break;
                                        }
                                    }
                                    if (!containStorage) {
                                        quotaStorage.setStorageSizeGBUsage(0.0);
                                    }
                                    quotaStorageList.add(quotaStorage);
                                }
                                quotaModel.getAllDataCenterStorages().setItems(quotaStorageList);
                            }
                        }), selectedDataCenter.getId());

                    }
                });
            }
        };

        GetQuotaByQuotaIdQueryParameters quotaParameters = new GetQuotaByQuotaIdQueryParameters();
        quotaParameters.setQuotaId(outer_quota.getId());
        Frontend.RunQuery(VdcQueryType.GetQuotaByQuotaId,
                quotaParameters,
                asyncQuery);

    }

    public void onRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> prms = new ArrayList<VdcActionParametersBase>();
        QuotaCRUDParameters crudParameters;
        for (Quota a : Linq.<Quota> Cast(getSelectedItems()))
        {
            crudParameters = new QuotaCRUDParameters();
            crudParameters.setQuotaId(a.getId());
            prms.add(crudParameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveQuota, prms,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        cancel();

                    }
                }, model);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeQuotasTitle());
        model.setHashName("remove_quota"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().quotasMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (Quota a : Linq.<Quota> Cast(getSelectedItems()))
        {
            list.add(a.getQuotaName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);
        if (command.equals(getCreateQuotaCommand())) {
            createQuota();
        }
        else if (command.equals(getEditQuotaCommand())) {
            editQuota();
        }
        else if (command.getName().equals("OnCreateQuota")) { //$NON-NLS-1$
            onCreateQuota();
        }
        else if (command.getName().equals("Cancel")) { //$NON-NLS-1$
            cancel();
        }
        else if (command.equals(getRemoveQuotaCommand())) {
            remove();
        }
        else if (command.getName().equals("OnRemove")) { //$NON-NLS-1$
            onRemove();
        }
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        if (systemTreeSelectedItem != value) {
            systemTreeSelectedItem = value;
            OnSystemTreeSelectedItemChanged();
        }
    }

    private void OnSystemTreeSelectedItemChanged() {
        Search();
    }

    @Override
    protected String getListName() {
        return "QuotaListModel"; //$NON-NLS-1$
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("quota"); //$NON-NLS-1$
    }

}
