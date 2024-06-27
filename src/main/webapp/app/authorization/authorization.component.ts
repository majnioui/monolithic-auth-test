import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AccountService } from 'app/core/auth/account.service';
import { AuthorizationService } from '../services/authorization.service';
import { Account } from 'app/core/auth/account.model';
import { HttpClient } from '@angular/common/http';
import { IDocker } from 'app/entities/docker/docker.model';

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
  selectedRepo: string | null = null;
  customBuildCommand: string = '';
  isBuildSuccessful: boolean = false;
  registryUsername: string = '';
  registryRepoName: string = '';
  dockerEntities: IDocker[] = [];
  selectedRegistry: 'docker' | 'quay' = 'docker';

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
      this.fetchDockerEntities();
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
      this.cloneSelectedRepository();
    } else {
      console.error('Repository name, user login, or platform is missing');
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
      this.AuthorizationService.executeBuildCommand(
        this.selectedRepo,
        this.userLogin,
        this.platform.toUpperCase(),
        this.customBuildCommand,
      ).subscribe({
        next: response => {
          console.log('Command execution started');
          this.isBuildSuccessful = true; // Set to true when build is successful
          // Handle response if needed
        },
        error: error => {
          console.error('Error executing command:', error);
          this.isBuildSuccessful = false; // Set to false if the build fails
        },
      });
    } else {
      console.error('Missing information for command execution');
      this.isBuildSuccessful = false;
    }
  }

  fetchDockerEntities() {
    this.http.get<IDocker[]>('/docker-entities').subscribe({
      next: entities => {
        this.dockerEntities = entities;
      },
      error: error => console.error('Error fetching Docker entities:', error),
    });
  }

  // Method to to trigger push to registry
  pushImageToRegistry(registryUsername: string, registryRepoName: string, selectedRegistry: string) {
    // Use either the selected entity values or the manually entered values
    const username = registryUsername;
    const repoName = registryRepoName;

    if (this.isBuildSuccessful && username && repoName && selectedRegistry) {
      const imageName = 'rkube-' + this.getFormattedDateTime(); // hardcoded the prefix rkube but it can be anything depends on the use case.
      this.AuthorizationService.pushToRegistry(imageName, username, repoName, selectedRegistry).subscribe({
        next: () => {
          console.log('Image pushed to registry successfully');
        },
        error: error => {
          console.error('Error pushing image to registry:', error);
        },
      });
    } else {
      console.error('Image build was not successful or missing parameters. Cannot push to registry.');
    }
  }
  onRegistryChange() {
    if (this.selectedRegistry === 'quay') {
      this.registryUsername = 'quay.io';
    } else {
      this.registryUsername = '';
    }
  }

  // Helper method to get the formatted date for the image name
  private getFormattedDateTime(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = (now.getMonth() + 1).toString().padStart(2, '0');
    const day = now.getDate().toString().padStart(2, '0');
    return `${year}${month}${day}`;
  }
}
