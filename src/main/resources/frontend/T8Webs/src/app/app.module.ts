// Angular
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FlexLayoutModule } from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Syncfusion
import { ButtonModule, CheckBoxModule } from "@syncfusion/ej2-angular-buttons";
import { TreeViewAllModule, ToolbarModule, MenuModule, ContextMenuModule  } from "@syncfusion/ej2-angular-navigations";
import { DropDownButtonModule } from "@syncfusion/ej2-angular-splitbuttons";
import { DialogModule } from '@syncfusion/ej2-angular-popups';
import { UploaderModule } from '@syncfusion/ej2-angular-inputs';
import { ChartModule } from '@syncfusion/ej2-angular-charts';
import { DashboardLayoutModule } from '@syncfusion/ej2-angular-layouts';
import { GridModule, GroupService, ToolbarService } from '@syncfusion/ej2-angular-grids';
import { DateTimeService, DateTimeCategoryService, AreaSeriesService, LegendService, TooltipService} from '@syncfusion/ej2-angular-charts';

// Additional Components
import { HomepageComponent } from "./homepage/homepage.component";
import { DashboardComponent } from './dashboard/dashboard.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { DashboardToolbarComponent } from './dashboard/dashboard-toolbar/dashboard-toolbar.component';
import { DashboardTreeComponent } from './dashboard/dashboard-tree/dashboard-tree.component';
import { ServerDialogComponent } from './dashboard/server-dialog/server-dialog.component';
import { ServerContentComponent } from './dashboard/server-content/server-content.component';
import { FileUploaderComponent } from './dashboard/server-content/content/file-uploader/file-uploader.component';
import { ServerInfoComponent } from './dashboard/server-content/content/server-info/server-info.component';
import { CpuGraphComponent } from './dashboard/server-content/content/cpu-graph/cpu-graph.component';
import { RamGraphComponent } from './dashboard/server-content/content/ram-graph/ram-graph.component';
import { NetGraphComponent } from './dashboard/server-content/content/net-graph/net-graph.component';
import { AdminServersComponent } from './dashboard/admin-servers/admin-servers.component';
import { ServersGridComponent } from './dashboard/admin-servers/servers-grid/servers-grid.component';
import { AdminUsersComponent } from './dashboard/admin-users/admin-users.component';
import { UsersGridComponent } from './dashboard/admin-users/users-grid/users-grid.component';

@NgModule({
  declarations: [
    AppComponent,
    HomepageComponent,
    DashboardComponent,
    PageNotFoundComponent,
    DashboardToolbarComponent,
    DashboardTreeComponent,
    ServerDialogComponent,
    ServerContentComponent,
    FileUploaderComponent,
    ServerInfoComponent,
    CpuGraphComponent,
    RamGraphComponent,
    NetGraphComponent,
    AdminServersComponent,
    ServersGridComponent,
    AdminUsersComponent,
    UsersGridComponent
  ],
  imports: [
    FlexLayoutModule,
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    ButtonModule,
    DropDownButtonModule,
    CheckBoxModule,
    ChartModule,
    DashboardLayoutModule,
    TreeViewAllModule,
    ToolbarModule,
    MenuModule,
    ContextMenuModule,
    DialogModule,
    FormsModule,
    ReactiveFormsModule,
    UploaderModule,
    GridModule
  ],
  providers: [DateTimeService, DateTimeCategoryService, AreaSeriesService, LegendService, TooltipService, GroupService, ToolbarService],
  bootstrap: [AppComponent]
})
export class AppModule { }
