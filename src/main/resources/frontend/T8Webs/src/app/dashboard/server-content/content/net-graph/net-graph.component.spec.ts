import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NetGraphComponent } from './net-graph.component';

describe('NetGraphComponent', () => {
  let component: NetGraphComponent;
  let fixture: ComponentFixture<NetGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NetGraphComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NetGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
