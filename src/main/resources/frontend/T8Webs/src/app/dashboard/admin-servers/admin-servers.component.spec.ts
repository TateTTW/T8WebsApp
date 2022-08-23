import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminServersComponent } from './admin-servers.component';

describe('AdminServersComponent', () => {
  let component: AdminServersComponent;
  let fixture: ComponentFixture<AdminServersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminServersComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminServersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
