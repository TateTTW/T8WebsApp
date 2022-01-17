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

  confirm() {
    const dialog = DialogUtility.confirm({
      title: "Confirm",
      content: "Are you sure you want to " + this.job.action + " " + this.job.type + " " + this.serverName?.value + "?",
      okButton: {
        text: "Continue",
        click: () => {
          this.submit();
          dialog.close();
        }
      },
      showCloseIcon: true
    });
  }

  private submit() {
    if(this.serverName && this.serverName.valid) {
      this.showSpinner.emit();

      if(this.job.action == JobAction.Add){
        this.addServer();
      }
      else if (this.job.action == JobAction.Rename){
        this.renameServer();
      }
    }
  }

  private addServer() {
    if(this.serverName && this.serverName.valid) {
      const serverName = this.serverName.value;
      this.addServerSub = this.dashboardService.addServer(serverName).subscribe(
        data => this.submitSuccess(serverName, data),
        error => this.submitFailure(error)
      )
    }
  }

  private renameServer() {
    if(this.serverName && this.serverName.valid) {
      const serverName = this.serverName.value;
      this.addServerSub = this.dashboardService.renameServer(this.job.vmid, serverName).subscribe(
        data => this.submitSuccess(serverName, data, this.job),
        error => this.submitFailure(error)
      )
    }
  }

  submitSuccess(serverName: string, data: any, job?: Job) {
    console.log(data);
    this.resetServerName();
    this.refreshTree.emit(job);
    this.hideSpinner.emit();
    this.close();

    DialogUtility.alert({
      title: 'Success',
      content: "Traffic to the " + serverName + " subdomain will be forwarded to the server on port 8080",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  submitFailure(data: any) {
    console.log(data);
    this.hideSpinner.emit();

    DialogUtility.alert({
      title: 'Error',
      content: "Failed to " + this.job.action + " " + this.job.type + ".",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  resetServerName() {
    this.serverName?.setValue("");
    this.serverName?.markAsUntouched();
  }

  close() {
    this.dialog.hide();
    this.resetServerName();
  }
}
