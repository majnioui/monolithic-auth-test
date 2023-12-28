import { Component, OnInit } from '@angular/core';
import { AccountService } from 'app/core/auth/account.service';
import { AuthorizationService } from '../services/authorization.service';
import { Account } from 'app/core/auth/account.model';

@Component({
  selector: 'app-test-github',
  templateUrl: './authorization.component.html',
  styleUrls: ['./authorization.component.scss'],
})
export class AuthorizationComponent implements OnInit {
  responseMessage: string = '';
  githubRepositories: any[] = [];
  gitlabRepositories: any[] = [];
  bitbucketRepositories: any[] = [];
  platform: 'github' | 'gitlab' | 'bitbucket' = 'github';
  urlValue: string = '';
  account: Account | null = null;
  userLogin: string | null = null;
  selectedJavaVersion: string | null = null;

  constructor(
    private accountService: AccountService,
    private AuthorizationService: AuthorizationService,
  ) {}

  ngOnInit() {
    this.accountService.getAuthenticationState().subscribe(account => {
      this.account = account;
      this.userLogin = account?.login || null;
      this.getGithubRepositories();
      this.getBitbucketRepositories();
    });
  }

  authorizePlatform(platform: string) {
    if (this.userLogin) {
      window.location.href = `/authorize-${platform}?userLogin=${this.userLogin}`;
    } else {
      console.error('User login is not available for authorization');
    }
  }

  getGithubRepositories() {
    this.AuthorizationService.getGithubRepositories().subscribe({
      next: repos => {
        this.githubRepositories = repos;
      },
      error: error => {
        console.error('Component error fetching GitHub repositories:', error);
      },
    });

    this.AuthorizationService.getGitLabRepositories().subscribe({
      next: repos => {
        this.gitlabRepositories = repos;
      },
      error: error => {
        console.error('Component error fetching GitLab repositories:', error);
      },
    });
  }

  getBitbucketRepositories() {
    this.AuthorizationService.getBitbucketRepositories().subscribe({
      next: repos => {
        this.bitbucketRepositories = repos;
      },
      error: error => {
        console.error('Component error fetching Bitbucket repositories:', error);
      },
    });
  }

  saveAndSwitchPlatform(newPlatform: 'github' | 'gitlab' | 'bitbucket') {
    this.AuthorizationService.saveClientUrl(this.urlValue, newPlatform).subscribe({
      next: () => {
        console.log('Client URL and platform type saved successfully');
      },
      error: error => {
        console.error('Error saving client URL and platform type', error);
      },
    });

    this.switchPlatform(newPlatform);
  }

  switchPlatform(newPlatform: 'github' | 'gitlab' | 'bitbucket') {
    this.platform = newPlatform;
  }

  noRepositoriesFor(platform: string): boolean {
    switch (platform) {
      case 'github':
        return this.githubRepositories.length === 0;
      case 'gitlab':
        return this.gitlabRepositories.length === 0;
      case 'bitbucket':
        return this.bitbucketRepositories.length === 0;
      default:
        return false;
    }
  }

  onRepositorySelect(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const repoName = selectElement.value;

    const clientId = '1';

    this.AuthorizationService.getJavaVersionFromGitHub(repoName, clientId).subscribe({
      next: version => {
        this.selectedJavaVersion = version;
      },
      error: error => {
        console.error('Error fetching Java version', error);
        this.selectedJavaVersion = 'Error fetching version';
      },
    });
  }
}
