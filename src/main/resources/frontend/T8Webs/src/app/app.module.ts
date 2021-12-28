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

// Additional Components
import { HomepageComponent } from "./homepage/homepage.component";
import { DashboardComponent } from './dashboard/dashboard.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { DashboardToolbarComponent } from './dashboard/dashboard-toolbar/dashboard-toolbar.component';
import { DashboardTreeComponent } from './dashboard/dashboard-tree/dashboard-tree.component';
import { ServerDialogComponent } from './dashboard/server-dialog/server-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    HomepageComponent,
    DashboardComponent,
    PageNotFoundComponent,
    DashboardToolbarComponent,
    DashboardTreeComponent,
    ServerDialogComponent
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
    TreeViewAllModule,
    ToolbarModule,
    MenuModule,
    ContextMenuModule,
    DialogModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
