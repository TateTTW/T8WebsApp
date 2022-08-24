import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {User, UserStatus} from "./dto/user";
import {NodeType, TreeNode} from "./dashboard-tree/TreeNode";
import {createSpinner, hideSpinner, showSpinner} from "@syncfusion/ej2-angular-popups";
import {Job, JobAction} from "./server-dialog/Job";
import {ServerDialogComponent} from "./server-dialog/server-dialog.component";
import {DashboardService} from "./dashboard.service";
import {Subscription} from "rxjs";
import {DialogUtility} from '@syncfusion/ej2-popups';
import {DashboardTreeComponent} from "./dashboard-tree/dashboard-tree.component";

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.less']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Subscriptions
  private requestAccessSub: Subscription | undefined;
  private serverStatusSub: Subscription | undefined;
  private startServerSub: Subscription | undefined;
  private stopServerSub: Subscription | undefined;
  private rebootServerSub: Subscription | undefined;
  private deleteServerSub: Subscription | undefined;
  // View Elements
  @ViewChild ('serverDialog') serverDialog!: ServerDialogComponent;
  @ViewChild ('dashboardTree') dashboardTree!: DashboardTreeComponent;

  user: User | undefined;
  selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.NONE);

  readonly  NodeType = NodeType;

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void { }

  ngOnDestroy(): void {
    this.requestAccessSub?.unsubscribe();
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
    if (!this.spinnerExist()) {
      createSpinner({ target:spinnerTarget, width: '70px' });
    }
    showSpinner(spinnerTarget);
  }

  hideSpinner() {
    if (this.spinnerExist()) {
      const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
      hideSpinner(spinnerTarget);
    }
  }

  private spinnerExist(): boolean {
    const spinnerTarget = document.querySelector('#spinnerDiv') as HTMLElement;
    const style = Array.from(spinnerTarget.children).reduce((acc: string[], node) => {
      node.classList.forEach(style => acc.push(style));
      return acc;
    }, []);
    return style.includes("e-spinner-pane");
  }

  nodeSelection(treeNode: TreeNode) {
    if(treeNode.type == NodeType.SERVER) {
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

  doJob(job: Job) {
    if(this.user?.status == UserStatus.REQUESTED){
      this.awaitingApprovalAlert();
      return;
    } else if(this.user?.status == UserStatus.NONE){
      this.confirmAccessRequest();
      return;
    }

    if(job.action == JobAction.Add || job.action == JobAction.Rename){
      this.processJob(job);
    }  else {
      this.confirmJob(job);
    }
  }

  private awaitingApprovalAlert() {
    DialogUtility.alert({
      title: "Awaiting Account Approval",
      content: "Your access request has not yet been approved.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  private confirmAccessRequest() {
    const dialog = DialogUtility.confirm({
      title: "Unauthorized User Request",
      content: "Your account has not been approved. Would you like to request access?",
      okButton: {
        text: "Yes",
        click: () => {
          this.requestAccessSub = this.dashboardService.requestAccess().subscribe(
            data => this.accessRequestSuccess(),
            error => this.accessRequestFailure(error)
          )
          dialog.close();
        }
      },
      showCloseIcon: true
    });
  }

  private accessRequestSuccess() {
    if (this.user) {
      this.user.status = UserStatus.REQUESTED;
    }
    DialogUtility.alert({
      title: "Success",
      content: "Your request has been submitted.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  private accessRequestFailure(error: any) {
    DialogUtility.alert({
      title: "Request Failed",
      content: "Your request failed to be submitted.",
      showCloseIcon: true,
      closeOnEscape: true,
      animationSettings: { effect: 'Zoom' }
    });
  }

  private confirmJob(job: Job) {
    const dialog = DialogUtility.confirm({
      title: "Confirm",
      content: "Are you sure you want to " + job.action + " " + job.type + "?",
      okButton: {
        text: "Continue",
        click: () => {
          this.processJob(job);
          dialog.close();
        }
      },
      showCloseIcon: true
    });
  }

  private processJob(job: Job) {
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

    if(job.action == JobAction.Delete){
      this.selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.NONE);
      this.dashboardTree.refresh();
    }

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
