import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './components/navbar/navbar';
import { AnimatedBackgroundComponent } from './shared/components/animated-background/animated-background.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Navbar, AnimatedBackgroundComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App {}
