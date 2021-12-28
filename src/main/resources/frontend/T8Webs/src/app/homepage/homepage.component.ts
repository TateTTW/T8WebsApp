import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {User} from "../dashboard/dto/user";
import {Subscription} from "rxjs";
import {DashboardService} from "../dashboard/dashboard.service";

@Component({
  selector: 'homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.less']
})
export class HomepageComponent implements OnInit, AfterViewInit, OnDestroy {
  // View Elements
  @ViewChild('container') container?: ElementRef;
  // Subscriptions
  private getUserSub: Subscription | undefined;

  private _user: User | undefined;
  set user(user: User | undefined) {
    this._user = user;
    this.retrievedUser.emit(user);
  }
  get user(): User | undefined {
    return this._user;
  }
  @Input() inDashboard = false;
  @Output() retrievedUser: EventEmitter<User> = new EventEmitter<User>();

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.getUserSub = this.dashboardService.getUser().subscribe(
      data => this.user = data,
      error => console.log(error)
    )
  }

  ngAfterViewInit(): void {
    if(this.inDashboard){
      this.container?.nativeElement.classList.add('dashboardContent');
    } else {
      this.container?.nativeElement.classList.add('fullscreen');
    }
  }

  ngOnDestroy(): void {
    this.getUserSub?.unsubscribe();
  }

  enterDashboard() {
    window.location.replace("/dashboard");
  }

  signIn() {
    window.location.replace("/oauth2/authorization/google");
  }
}
