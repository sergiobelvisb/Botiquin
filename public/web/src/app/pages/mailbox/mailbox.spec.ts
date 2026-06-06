import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Mailbox } from './mailbox';

describe('Mailbox', () => {
  let component: Mailbox;
  let fixture: ComponentFixture<Mailbox>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mailbox],
    }).compileComponents();

    fixture = TestBed.createComponent(Mailbox);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
