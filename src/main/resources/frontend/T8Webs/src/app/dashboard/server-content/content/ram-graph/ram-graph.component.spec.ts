import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RamGraphComponent } from './ram-graph.component';

describe('RamGraphComponent', () => {
  let component: RamGraphComponent;
  let fixture: ComponentFixture<RamGraphComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RamGraphComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RamGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
