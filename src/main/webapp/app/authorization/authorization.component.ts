import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AccountService } from 'app/core/auth/account.service';
import { AuthorizationService } from '../services/authorization.service';
import { Account } from 'app/core/auth/account.model';
import { HttpClient } from '@angular/common/http';

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

  selectedGithubRepo: string | null = null;
  suggestedBuildpack: string | null = null;
  selectedRepo: string | null = null;
  customBuildCommand: string = '';

  constructor(
    private accountService: AccountService,
    private AuthorizationService: AuthorizationService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
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

  onRepoSelected() {
    if (this.selectedRepo && this.userLogin && this.platform) {
      this.AuthorizationService.getSuggestedBuildpack(this.selectedRepo, this.userLogin, this.platform.toUpperCase()).subscribe({
        next: response => {
          this.suggestedBuildpack = response.buildpack;
          this.cloneSelectedRepository();
          this.cdr.detectChanges();
        },
        error: error => {
          console.error('Error fetching suggested buildpack:', error);
          this.suggestedBuildpack = null;
        },
      });
    } else {
      console.error('Repository name, user login, or platform is missing');
      this.suggestedBuildpack = null;
    }
  }

  private cloneSelectedRepository() {
    if (this.selectedRepo && this.userLogin && this.platform) {
      this.AuthorizationService.cloneRepository(this.selectedRepo, this.userLogin, this.platform).subscribe({
        next: () => console.log('Repository cloning started'),
        error: error => console.error('Error cloning repository:', error),
      });
    } else {
      console.error('Repository name, user login, or platform is missing');
    }
  }

  // Method to get the repository list based on the selected platform
  getRepositoryList() {
    switch (this.platform) {
      case 'github':
        return this.githubRepositories;
      case 'gitlab':
        return this.gitlabRepositories;
      case 'bitbucket':
        return this.bitbucketRepositories;
      default:
        return [];
    }
  }

  // Method to handle the custom build command execution
  executeCustomBuildCommand() {
    if (this.selectedRepo && this.userLogin && this.platform && this.customBuildCommand) {
      // Call a new backend service method to execute the custom build command
      this.AuthorizationService.executeBuildCommand(this.selectedRepo, this.userLogin, this.platform, this.customBuildCommand).subscribe({
        next: () => console.log('Custom build command executed'),
        error: error => console.error('Error executing custom build command:', error),
      });
    } else {
      console.error('Required data is missing for executing the custom build command');
    }
  }
}
