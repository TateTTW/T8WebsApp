import {Component, OnInit, ViewChild} from '@angular/core';
import {GridComponent, SelectionSettingsModel} from "@syncfusion/ej2-angular-grids";
import {DashboardService} from "../../dashboard.service";
import {UserStatus} from "../../dto/user";

@Component({
  selector: 'users-grid',
  templateUrl: './users-grid.component.html',
  styleUrls: ['./users-grid.component.less']
})
export class UsersGridComponent implements OnInit {
// View Elements
  @ViewChild('grid') grid?: GridComponent;

  data: any = [];
  groupOptions = { showDropArea: false, columns: ["status"] };
  selectionOptions: SelectionSettingsModel = { type: 'Single' };
  toolbar = [
    { text: 'Grant', prefixIcon: 'fa fa-check', id: 'grant', align: 'Left', disabled: 'true' },
    { text: 'Revoke', prefixIcon: 'fa fa-times', id: 'revoke', align: 'Left', disabled: 'true' }
    ];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.getAllUsers();
  }

  private async getAllUsers() {
    const response =  await this.dashboardService.getAllUsers().toPromise();
    this.data = response ?? [];
  }

  toolbarClick(event: any) {
    const selectedRows = this.grid?.getSelectedRecords() ?? [];
    if (event?.item?.properties?.id && selectedRows.length > 0 && selectedRows[0].hasOwnProperty("userId")) {
      // @ts-ignore
      const userId = selectedRows[0].userId;
      const toolbarItem = event.item.properties.id;
      if (toolbarItem == "grant") {
        this.grantAccess(userId);
      } else if (toolbarItem == "revoke") {
        this.revokeAccess(userId);
      }
    }
  }

  rowSelected(event: any) {
    if (event?.data?.status) {
      const userStatus = event.data.status;
      if (userStatus == UserStatus.APPROVED) {
        this.grid?.toolbarModule.enableItems(["revoke"], true);
      } else if (userStatus == UserStatus.REQUESTED || userStatus == UserStatus.NONE) {
        this.grid?.toolbarModule.enableItems(["grant"], true);
      }
    }
  }

  rowDeselected(event: any) {
    this.grid?.toolbarModule.enableItems(["grant", "revoke"], false);
  }

  private async grantAccess(userId: string) {
    this.grid?.showSpinner();
    const response = await this.dashboardService.grantAccess(userId).toPromise();
    await this.getAllUsers();
    this.grid?.hideSpinner();
  }

  private async revokeAccess(userId: string) {
    this.grid?.showSpinner();
    const response = await this.dashboardService.revokeAccess(userId).toPromise();
    await this.getAllUsers();
    this.grid?.hideSpinner();
  }

}
