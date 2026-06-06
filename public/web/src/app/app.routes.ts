import { Routes } from '@angular/router';
import { Layout } from './layout/layout';

export const routes: Routes = [
  {
    path: '',
    component: Layout,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/home/home').then(m => m.Home)
      },
      {
        path: 'about',
        loadComponent: () =>
          import('./pages/about/about').then(m => m.About)
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./pages/login/login').then(m => m.Login)
      },
      {
        path: 'mailbox',
        loadComponent: () =>
          import('./pages/mailbox/mailbox').then(m => m.Mailbox)
      }
    ]
  }
];