import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServersGridComponent } from './servers-grid.component';

describe('ServersGridComponent', () => {
  let component: ServersGridComponent;
  let fixture: ComponentFixture<ServersGridComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServersGridComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServersGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
