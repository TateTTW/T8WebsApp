import {Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {DashboardService} from "../dashboard.service";
import {Subscription} from "rxjs";
import {DialogComponent} from "@syncfusion/ej2-angular-popups";
import {AbstractControl, FormControl, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {Job, JobAction, JobType} from "./Job";

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
      this.addServerSub = this.dashboardService.addServer(this.serverName.value).subscribe(
        data => this.submitSuccess(data),
        error => this.submitFailure(error)
      )
    }
  }

  submitSuccess(data: any): void {
    console.log(data);
    this.resetServerName();
    this.hideSpinner.emit();
  }

  submitFailure(data: any): void {
    console.log(data);
    this.hideSpinner.emit();
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
