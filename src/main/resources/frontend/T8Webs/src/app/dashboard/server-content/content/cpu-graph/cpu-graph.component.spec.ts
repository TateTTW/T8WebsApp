import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CpuGraphComponent } from './cpu-graph.component';

describe('CpuGraphComponent', () => {
  let component: CpuGraphComponent;
  let fixture: ComponentFixture<CpuGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CpuGraphComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CpuGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
