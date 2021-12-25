// Angular
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Syncfusion
import { ButtonModule, CheckBoxModule } from "@syncfusion/ej2-angular-buttons";

// Additional Components
import { DashboardComponent } from './dashboard/dashboard.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ButtonModule,
    CheckBoxModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
