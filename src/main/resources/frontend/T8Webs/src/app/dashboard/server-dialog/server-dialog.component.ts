import {Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {DashboardService} from "../dashboard.service";
import {Subscription} from "rxjs";
import {DialogComponent} from "@syncfusion/ej2-angular-popups";
import {AbstractControl, FormControl, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {Job, JobAction, JobType} from "./Job";
import {DialogUtility} from "@syncfusion/ej2-popups";

@Component({
  selector: 'server-dialog',
  templateUrl: './server-dialog.component.html',
  styleUrls: ['./server-dialog.component.less']
})
export class ServerDialogComponent implements OnInit, OnDestroy {
  // View Elements
  @ViewChild ('dialog') dialog!: DialogComponent
  // Subscriptions
  private addServerSub: Subscription | undefined;

  public job: Job = {
    type: JobType.None,
    action: JobAction.None,
    vmid: -1
  }

  @Output() showSpinner: EventEmitter<any> = new EventEmitter<any>();
  @Output() hideSpinner: EventEmitter<any> = new EventEmitter<any>();
  @Output() refreshTree: EventEmitter<Job> = new EventEmitter<Job>();

  formGroup = new FormGroup({
    serverName: new FormControl(null, [
      Validators.required,
      Validators.minLength(2),
      Validators.maxLength(20),
      Validators.pattern('[a-zA-Z0-9]*')
    ])
  })

  private _serverName: AbstractControl | null = null;
  get serverName(): AbstractControl | null {
    return this.formGroup.get("serverName");
  }
  private _errors: ValidationErrors = this.serverName?.errors ?? {};
  get errors(): ValidationErrors {
    return this.serverName?.errors ?? {};
  }

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void { }

  ngOnDestroy(): void {
    this.addServerSub?.unsubscribe();
  }

  submit(): void {
    if(this.serverName && this.serverName.valid) {
      this.showSpinner.emit();

      if(this.job.action == JobAction.Add){
        this.addServer();
      }
      else if (this.job.action == JobAction.Rename){

      }
    }
  }

  private addServer(): void {
    if(this.serverName && this.serverName.valid) {
      const serverName = this.serverName.value;
      this.addServerSub = this.dashboardService.addServer(serverName).subscribe(
        data => this.submitSuccess(serverName, data),
        error => this.submitFailure(error)
      )
    }
  }

  submitSuccess(serverName: string, data: any): void {
    console.log(data);
    this.resetServerName();
    this.hideSpinner.emit();
    this.refreshTree.emit();

    DialogUtility.alert({
      title: 'Success',
      content: "Traffic to the " + serverName + " subdomain will be forwarded to the server on port 8080",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  submitFailure(data: any): void {
    console.log(data);
    this.hideSpinner.emit();

    DialogUtility.alert({
      title: 'Success',
      content: "Traffic to the " + this.serverName?.value + " subdomain will be forwarded to the server on port 8080",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  resetServerName(): void {
    this.serverName?.setValue("");
    this.serverName?.markAsUntouched();
  }

  close(): void {
    this.dialog.hide();
    this.resetServerName();
  }
}
