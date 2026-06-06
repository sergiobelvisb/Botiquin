import { Component } from '@angular/core';
import { RouterOutlet, RouterLinkWithHref, RouterLink } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLinkWithHref, RouterLink],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class Layout {}