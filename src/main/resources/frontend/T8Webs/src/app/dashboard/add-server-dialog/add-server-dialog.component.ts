import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {DashboardService} from "../dashboard.service";
import {Subscription} from "rxjs";
import {DialogComponent} from "@syncfusion/ej2-angular-popups";

@Component({
  selector: 'add-server-dialog',
  templateUrl: './add-server-dialog.component.html',
  styleUrls: ['./add-server-dialog.component.less']
})
export class AddServerDialogComponent implements OnInit, OnDestroy {
  // View Elements
  @ViewChild ('dialog') dialog!: DialogComponent
  @ViewChild ('serverName') serverName!: ElementRef;
  // Subscriptions
  private addServerSub: Subscription | undefined;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void { }

  ngOnDestroy(): void {
    this.addServerSub?.unsubscribe();
  }

  submit(): void {
    const serverName = this.serverName.nativeElement.value;

    this.addServerSub = this.dashboardService.addServer(serverName).subscribe(
      data => console.log(data),
      error => console.log(error)
    )
  }

  close(): void {
    this.dialog.hide();
  }
}
