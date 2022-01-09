import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {User} from "./dto/user";
import {NodeType, TreeNode} from "./dashboard-tree/TreeNode";
import {createSpinner, hideSpinner, showSpinner} from "@syncfusion/ej2-angular-popups";
import {Job, JobAction, JobType} from "./server-dialog/Job";
import {ServerDialogComponent} from "./server-dialog/server-dialog.component";
import {DashboardService} from "./dashboard.service";
import {Subscription} from "rxjs";
import {DialogUtility} from '@syncfusion/ej2-popups';

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.less']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  // Subscriptions
  private serverStatusSub: Subscription | undefined;
  private startServerSub: Subscription | undefined;
  private stopServerSub: Subscription | undefined;
  private rebootServerSub: Subscription | undefined;
  private deleteServerSub: Subscription | undefined;
  // View Elements
  @ViewChild ('serverDialog') serverDialog!: ServerDialogComponent;

  user: User | undefined;
  selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.None);

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void { }

  ngAfterViewInit(): void {
    setTimeout((()=>{
      const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
      createSpinner({ target:spinnerTarget, width: '70px' });
    }).bind(this), 1000);
  }

  ngOnDestroy(): void {
    this.startServerSub?.unsubscribe();
    this.stopServerSub?.unsubscribe();
    this.rebootServerSub?.unsubscribe();
    this.deleteServerSub?.unsubscribe();
    this.serverStatusSub?.unsubscribe();
  }

  setUser(user: User) {
    this.user = user;
  }

  showSpinner() {
    const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
    showSpinner(spinnerTarget);
  }

  hideSpinner() {
    const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
    hideSpinner(spinnerTarget);
  }

  nodeSelection(treeNode: TreeNode) {
    if(treeNode.type == NodeType.Server) {
      this.getServerStatus(treeNode);
    }
    this.selectedTreeNode = treeNode;
  }

  private getServerStatus(treeNode: TreeNode) {
    // setTimeout(()=>{
    //   const status = 'stopped'
    //   if(this.selectedTreeNode.id === treeNode.id){
    //     this.selectedTreeNode = new TreeNode(treeNode.id, treeNode.name, status, treeNode.type);
    //   }
    // }, 1000);
    this.serverStatusSub = this.dashboardService.getServerStatus(treeNode.id).subscribe(
      data =>  this.getStatusSuccess(treeNode, data),
      error => console.log(error)
    );
  }

  private getStatusSuccess(treeNode: TreeNode, data: any) {
    const status = data?.status;

    if(status && this.selectedTreeNode.id === treeNode.id){
      this.selectedTreeNode = new TreeNode(treeNode.id, treeNode.name, status, treeNode.type);
    }
  }

  processJob(job: Job) {
    switch (job.action) {
      case JobAction.Add:
      case JobAction.Rename:
        this.openServerDialog(job);
        break;
      case JobAction.Start:
        this.startServer(job);
        break;
      case JobAction.Stop:
        this.stopServer(job);
        break;
      case JobAction.Reboot:
        this.rebootServer(job);
        break;
      case JobAction.Delete:
        this.deleteServer(job)
        break;
    }
  }

  private openServerDialog(job: Job) {
    this.serverDialog.job = job;
    this.serverDialog.dialog.show();
  }

  private startServer(job: Job) {
    this.showSpinner();
    this.startServerSub = this.dashboardService.startServer(job.vmid).subscribe(
      data => this.handleSuccessfulJob(job, data),
      error => this.handleFailedJob(job, error)
    )
  }

  private stopServer(job: Job) {
    this.showSpinner();
    this.stopServerSub = this.dashboardService.stopServer(job.vmid).subscribe(
      data => this.handleSuccessfulJob(job, data),
      error => this.handleFailedJob(job, error)
    )
  }

  private rebootServer(job: Job) {
    this.showSpinner();
    this.rebootServerSub = this.dashboardService.rebootServer(job.vmid).subscribe(
      data => this.handleSuccessfulJob(job, data),
      error => this.handleFailedJob(job, error)
    )
  }

  private deleteServer(job: Job) {
    this.showSpinner();
    this.rebootServerSub = this.dashboardService.deleteServer(job.vmid).subscribe(
      data => this.handleSuccessfulJob(job, data),
      error => this.handleFailedJob(job, error)
    )
  }

  private handleSuccessfulJob(job: Job, data: any) {
    const status = data?.status;

    if(status && job.vmid == this.selectedTreeNode.id){
      this.selectedTreeNode = new TreeNode(this.selectedTreeNode.id, this.selectedTreeNode.name, status, this.selectedTreeNode.type);
    }

    this.hideSpinner();

    DialogUtility.alert({
      title: 'Success',
      content: job.action + " " + job.type + " completed.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  private handleFailedJob(job: Job, error: any) {
    if(job.vmid == this.selectedTreeNode.id){
      this.getServerStatus(this.selectedTreeNode);
    }

    this.hideSpinner();

    DialogUtility.alert({
      title: 'Error',
      content: "Failed to " + job.action + " " + job.type + ".",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }
}
