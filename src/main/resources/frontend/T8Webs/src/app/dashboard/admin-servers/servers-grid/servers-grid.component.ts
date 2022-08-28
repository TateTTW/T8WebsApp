import {Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {EditSettingsModel, GridComponent, SelectionSettingsModel, ToolbarItems} from "@syncfusion/ej2-angular-grids";
import {Subscription} from "rxjs";
import {DashboardService} from "../../dashboard.service";
import {DialogUtility} from "@syncfusion/ej2-popups";

@Component({
  selector: 'servers-grid',
  templateUrl: './servers-grid.component.html',
  styleUrls: ['./servers-grid.component.less']
})
export class ServersGridComponent implements OnInit, OnDestroy {
  // View Elements
  @ViewChild('grid') grid?: GridComponent;
  // Subscriptions
  private deleteServerSub: Subscription | undefined;

  @Output() rowSelectedEvent: EventEmitter<number> = new EventEmitter<number>();
  @Output() rowDeselectedEvent: EventEmitter<any> = new EventEmitter<any>();
  @Output() systemDataClicked: EventEmitter<any> = new EventEmitter<any>();
  @Output() refreshTree: EventEmitter<any> = new EventEmitter<any>();

  data: any = [];
  groupOptions = { showDropArea: false, columns: ["creationStatus", "userId"] };
  selectionOptions: SelectionSettingsModel = { type: 'Single' };
  editSettings: EditSettingsModel = { allowEditing: true, allowAdding: false, allowDeleting: true };
  toolbar = [
    { text: 'System Data', prefixIcon: 'fa fa-server', id: 'system', align: 'Left', disabled: true },
    { text: 'Delete', prefixIcon: 'fa fa-trash-alt', id: 'delete', align: 'Right', disabled: true },
    { text: 'Refresh', prefixIcon: 'fa fa-sync-alt', id: 'refresh', align: 'Right', disabled: false }
    ];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.getGridData();
  }

  ngOnDestroy(): void {
    this.deleteServerSub?.unsubscribe();
  }

  private async getGridData() {
    const response = await this.dashboardService.getAllServers().toPromise();
    if (response && Array.isArray(response)) {
      this.data = response.map(server => {
        if (server.creationStatus == "NONE" || server.creationStatus == "BEGIN") {
          server.creationStatus = "IN PROGRESS";
        } else if (server.creationStatus != "COMPLETED") {
          server.creationStatus = "FAILED";
        }
        return server;
      })
    }
  }

  toolbarClick(event: any) {
    if (event?.item?.properties?.id) {
      const toolbarItem = event.item.properties.id;
      const selectedRows = this.grid?.getSelectedRecords() ?? [];
      if (toolbarItem == "system") {
        this.grid?.toolbarModule.enableItems(["system"], false);
        this.grid?.selectionModule.clearRowSelection();
        this.systemDataClicked.emit();
      } else if (toolbarItem == "refresh") {
        this.getGridData();
      } else if (toolbarItem == "delete"
        && selectedRows.length > 0
        && selectedRows[0].hasOwnProperty("vmid")
        && selectedRows[0].hasOwnProperty("name"))
      {
        // @ts-ignore
        this.confirmDelete(selectedRows[0].vmid, selectedRows[0].name);
      }
    }
  }

  rowSelected(event: any) {
    if (event?.data?.creationStatus && event.data.creationStatus != "IN PROGRESS") {
      this.grid?.toolbarModule.enableItems(["delete"], true);
    }
    this.grid?.toolbarModule.enableItems(["system"], true);
    this.rowSelectedEvent.emit(event);
  }

  rowDeselected(event: any) {
    this.rowDeselectedEvent.emit(event);
    this.grid?.toolbarModule.enableItems(["delete"], false);
  }

  private confirmDelete(vmid: number, name: string) {
    const dialog = DialogUtility.confirm({
      title: "Confirm",
      content: "Are you sure you want to delete " + name + "(" + vmid + ")?",
      okButton: {
        text: "Continue",
        click: () => {
          dialog.close();
          this.grid?.showSpinner();
          this.deleteServerSub = this.dashboardService.forceDeleteServer(vmid).subscribe(
            data => this.deleteSuccess(),
            error => this.deleteFailureAlert()
          )
        }
      },
      showCloseIcon: true
    });
  }

  private async deleteSuccess() {
    this.refreshTree.emit();
    await this.getGridData();
    this.grid?.hideSpinner();
  }

  private deleteFailureAlert() {
    this.grid?.hideSpinner();
    DialogUtility.alert({
      title: "Error",
      content: "Failed to delete server.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }
}

