package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class LiveMigrateVmDisksParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -4601615377848349051L;

    private List<LiveMigrateDiskParameters> parametersList;
    private Guid vmId;
    private LiveMigrateStage stage = LiveMigrateStage.CREATE_SNAPSHOT;

    public LiveMigrateVmDisksParameters() {
        // Empty constructor for serializing / deserializing
    }

    public LiveMigrateVmDisksParameters(List<LiveMigrateDiskParameters> parametersList, Guid vmId) {
        this.parametersList = parametersList;
        this.vmId = vmId;
    }

    public List<LiveMigrateDiskParameters> getParametersList() {
        return parametersList;
    }

    public void setParametersList(List<LiveMigrateDiskParameters> parametersList) {
        this.parametersList = parametersList;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public LiveMigrateStage getStage() {
        return stage;
    }

    public void setStage(LiveMigrateStage stage) {
        this.stage = stage;
    }

    public enum LiveMigrateStage {
        CREATE_SNAPSHOT,
        LIVE_MIGRATE_DISK_EXEC_START,
        LIVE_MIGRATE_DISK_EXEC_COMPLETED
    }
}

