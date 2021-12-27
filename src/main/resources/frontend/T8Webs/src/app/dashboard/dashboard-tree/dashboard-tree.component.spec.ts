import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardTreeComponent } from './dashboard-tree.component';

describe('DashboardTreeComponent', () => {
  let component: DashboardTreeComponent;
  let fixture: ComponentFixture<DashboardTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DashboardTreeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
