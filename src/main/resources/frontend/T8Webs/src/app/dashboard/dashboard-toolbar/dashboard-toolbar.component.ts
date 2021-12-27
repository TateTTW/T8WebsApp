import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ToolbarComponent, MenuAnimationSettingsModel} from "@syncfusion/ej2-angular-navigations";
import { ItemModel, MenuEventArgs } from '@syncfusion/ej2-angular-splitbuttons';
import {DashboardService} from "../dashboard.service";
import {User} from "../dto/user";
import {Subscription} from "rxjs";

@Component({
  selector: 'dashboard-toolbar',
  templateUrl: './dashboard-toolbar.component.html',
  styleUrls: ['./dashboard-toolbar.component.less']
})
export class DashboardToolbarComponent implements OnInit, OnDestroy {
  // View elements
  @ViewChild('toolbar') toolbarObj?: ToolbarComponent;
  // Subscriptions
  private getUserSub: Subscription | undefined;

  user: User | undefined;

  public userMenuItems: ItemModel[] = [
    {
      text: 'Log Out'
    }];


  public menuItems: { [key: string]: Object }[] = [
    {
      text: 'Start'
    },
    {
      text: 'Stop'
    },
    {
      text: 'Reboot'
    },
    {
      text: 'Rename'
    }
  ];

  public menuFields: Object = {
    text: ['header', 'text', 'value'],
    children: ['subItems', 'options']
  };

  public animationSettings: MenuAnimationSettingsModel = { effect: 'None' };

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.getUserSub = this.dashboardService.getUser().subscribe(
      data => this.user = data,
      error => console.log(error)
    )
  }

  ngOnDestroy(): void {
    this.getUserSub?.unsubscribe();
  }

  public created(): void {
    this.toolbarObj?.refreshOverflow();
  }

  public menuSelect(args: MenuEventArgs): void {
    switch (args.item.text) {

    }
    console.log(args.item.text);
  }

  public userMenuSelect(args: Event): void {
    this.logout();
  }

  public login(){
    window.location.replace("/oauth2/authorization/google");
  }

  public logout() {
    window.location.replace("/logout");
  }
}
