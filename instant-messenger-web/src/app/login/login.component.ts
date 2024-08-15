import { Component, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  credentials = { nim: '', password: '' };

  constructor(
    private userService: UserService,
    private router: Router,
  ) {}

  login(): void {
    this.userService.login(this.credentials).subscribe(
      (response: any) => {
        localStorage.setItem('credentials', JSON.stringify(response?.data));
        // Handle successful login (e.g., store token, redirect)
        this.router.navigate(['/chat']);
      },
      (error: any) => {
        // Handle error
        console.error('Login failed', error);
      }
    );
  }
}
