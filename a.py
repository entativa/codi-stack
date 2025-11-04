#!/usr/bin/env python3
"""
Clean Clone Script for CodiBase Stack
Clones OneDev, VS Code, and Tabby without git history/submodules
Makes them ready for YOUR git repo as fresh codebases
"""

import subprocess
import shutil
import os
import sys
from pathlib import Path

# Repository configurations
REPOS = {
    "onedev": {
        "url": "https://github.com/theonedev/onedev.git",
        "target_name": "codibase",
        "branch": "main"
    },
    "vscode": {
        "url": "https://github.com/microsoft/vscode.git",
        "target_name": "vibecoda",
        "branch": "main"
    },
    "tabby": {
        "url": "https://github.com/TabbyML/tabby.git",
        "target_name": "pilotcodi",
        "branch": "main"
    }
}

class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

def log(message, color=Colors.BLUE):
    print(f"{color}{message}{Colors.RESET}")

def log_success(message):
    print(f"{Colors.GREEN}✓ {message}{Colors.RESET}")

def log_error(message):
    print(f"{Colors.RED}✗ {message}{Colors.RESET}")

def log_warning(message):
    print(f"{Colors.YELLOW}⚠ {message}{Colors.RESET}")

def run_command(cmd, cwd=None, capture=False):
    """Run shell command and handle errors"""
    try:
        if capture:
            result = subprocess.run(
                cmd,
                shell=True,
                cwd=cwd,
                capture_output=True,
                text=True,
                check=True
            )
            return result.stdout.strip()
        else:
            subprocess.run(cmd, shell=True, cwd=cwd, check=True)
        return True
    except subprocess.CalledProcessError as e:
        log_error(f"Command failed: {cmd}")
        if capture and e.stderr:
            log_error(f"Error: {e.stderr}")
        return False

def check_prerequisites():
    """Check if required tools are installed"""
    log(f"\n{Colors.BOLD}Checking prerequisites...{Colors.RESET}")
    
    if not shutil.which("git"):
        log_error("Git is not installed. Please install git first.")
        sys.exit(1)
    
    log_success("Git is installed")

def clean_git_completely(target_dir):
    """Remove ALL git-related files and folders including LFS"""
    log(f"  → Removing all git history and metadata...")
    
    # Remove .git directory
    git_dir = target_dir / ".git"
    if git_dir.exists():
        shutil.rmtree(git_dir)
        log_success(f"    Removed .git directory")
    
    # Remove .gitignore, .gitattributes, .gitmodules, and any LFS files
    git_files = [".gitignore", ".gitattributes", ".gitmodules", ".git", ".lfsconfig"]
    for git_file in git_files:
        file_path = target_dir / git_file
        if file_path.exists():
            if file_path.is_file():
                file_path.unlink()
            else:
                shutil.rmtree(file_path)
            log_success(f"    Removed {git_file}")
    
    # Remove any .git* files at root level
    for git_item in target_dir.glob(".git*"):
        if git_item.is_file():
            git_item.unlink()
            log_success(f"    Removed {git_item.name}")
        elif git_item.is_dir():
            shutil.rmtree(git_item)
            log_success(f"    Removed {git_item.name}/")
    
    # Find and remove any nested .git directories (submodules)
    for git_subdir in target_dir.rglob(".git"):
        if git_subdir.is_dir():
            shutil.rmtree(git_subdir)
            log_success(f"    Removed nested .git at {git_subdir.relative_to(target_dir)}")
        elif git_subdir.is_file():
            git_subdir.unlink()
            log_success(f"    Removed .git file at {git_subdir.relative_to(target_dir)}")
    
    # Remove any nested .gitattributes (LFS tracking files)
    for git_attr in target_dir.rglob(".gitattributes"):
        git_attr.unlink()
        log_success(f"    Removed nested .gitattributes at {git_attr.relative_to(target_dir)}")

def clone_repo_clean(repo_key, config, workspace_dir):
    """Clone repository and strip all git history"""
    url = config["url"]
    target_name = config["target_name"]
    branch = config["branch"]
    
    log(f"\n{Colors.BOLD}{'='*60}{Colors.RESET}")
    log(f"{Colors.BOLD}Processing: {repo_key.upper()} → {target_name}{Colors.RESET}")
    log(f"{Colors.BOLD}{'='*60}{Colors.RESET}")
    
    temp_clone_dir = workspace_dir / f"_temp_{target_name}"
    target_dir = workspace_dir / target_name
    
    # Clean up if exists
    if temp_clone_dir.exists():
        log_warning(f"Removing existing temp directory: {temp_clone_dir}")
        shutil.rmtree(temp_clone_dir)
    
    if target_dir.exists():
        log_warning(f"Target directory already exists: {target_dir}")
        response = input(f"  Remove and re-clone? (y/n): ").lower()
        if response == 'y':
            shutil.rmtree(target_dir)
            log_success(f"Removed {target_dir}")
        else:
            log_warning(f"Skipping {target_name}")
            return False
    
    # Shallow clone to temp directory
    # Skip LFS files to avoid GitHub bandwidth limits - we don't need model weights for code work
    log(f"Shallow cloning from {url} (skipping LFS files)...")
    
    # Set GIT_LFS_SKIP_SMUDGE to skip downloading LFS files
    env = os.environ.copy()
    env['GIT_LFS_SKIP_SMUDGE'] = '1'
    
    clone_cmd = f'git clone --depth 1 --branch {branch} --single-branch "{url}" "{temp_clone_dir}"'
    
    try:
        subprocess.run(
            clone_cmd,
            shell=True,
            cwd=workspace_dir,
            env=env,
            check=True
        )
    except subprocess.CalledProcessError:
        log_error(f"Failed to clone {repo_key}")
        return False
    
    log_success(f"Cloned successfully to temp directory (LFS files skipped)")
    
    # Remove ALL git-related content
    clean_git_completely(temp_clone_dir)
    
    # Move to final location
    log(f"Moving to final location: {target_name}")
    shutil.move(str(temp_clone_dir), str(target_dir))
    log_success(f"Moved to {target_dir}")
    
    # Verify no git artifacts remain (including LFS)
    git_check = list(target_dir.rglob(".git*"))
    lfs_check = list(target_dir.rglob(".lfs*"))
    all_artifacts = git_check + lfs_check
    
    if all_artifacts:
        log_warning(f"Found remaining git/LFS artifacts: {len(all_artifacts)} files")
        for artifact in all_artifacts:
            try:
                if artifact.is_dir():
                    shutil.rmtree(artifact)
                else:
                    artifact.unlink()
            except Exception as e:
                log_warning(f"Could not remove {artifact}: {e}")
        log_success("Cleaned up remaining artifacts")
    else:
        log_success("No git/LFS artifacts found - completely clean!")
    
    # Double-check for any LFS pointer files and remove them
    log(f"  → Scanning for LFS pointer files...")
    lfs_pointers_found = 0
    for file_path in target_dir.rglob("*"):
        if file_path.is_file():
            try:
                # Check if file is an LFS pointer (small file with "version https://git-lfs.github.com/spec")
                if file_path.stat().st_size < 200:  # LFS pointers are typically ~130 bytes
                    content = file_path.read_text(errors='ignore')
                    if 'version https://git-lfs.github.com/spec' in content:
                        file_path.unlink()
                        lfs_pointers_found += 1
            except:
                pass
    
    if lfs_pointers_found > 0:
        log_success(f"    Removed {lfs_pointers_found} LFS pointer files")
    
    # Get directory size
    total_size = sum(f.stat().st_size for f in target_dir.rglob('*') if f.is_file())
    size_mb = total_size / (1024 * 1024)
    
    log_success(f"✓ {target_name} ready! Size: {size_mb:.1f} MB")
    
    return True

def create_workspace_readme(workspace_dir):
    """Create README for the workspace"""
    readme_content = """# CodiBase Stack Workspace

This workspace contains the three core components of the CodiBase stack:

## Components

1. **codibase/** - Git hosting, CI/CD, project management (forked from OneDev)
2. **vibecoda/** - Code editor for vibe coding (forked from VS Code)
3. **pilotcodi/** - AI code completion (forked from Tabby)

## Setup

Each component is a clean codebase without git history. You can now:

1. Initialize git in this workspace:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: CodiBase stack"
   ```

2. Add your remote:
   ```bash
   git remote add origin YOUR_REPO_URL
   git push -u origin main
   ```

3. Start building! 🔥

## Next Steps

- Rebrand each component with CodiBase branding
- Set up development environments
- Configure integrations between components
- Build the hybrid AI routing system

---
Built with vibe 🚀
"""
    
    readme_path = workspace_dir / "README.md"
    readme_path.write_text(readme_content)
    log_success(f"Created workspace README: {readme_path}")

def main():
    log(f"\n{Colors.BOLD}{Colors.GREEN}╔═══════════════════════════════════════════════╗{Colors.RESET}")
    log(f"{Colors.BOLD}{Colors.GREEN}║  CodiBase Stack - Clean Clone Script         ║{Colors.RESET}")
    log(f"{Colors.BOLD}{Colors.GREEN}╚═══════════════════════════════════════════════╝{Colors.RESET}\n")
    
    # Check prerequisites
    check_prerequisites()
    
    # Get workspace directory
    default_workspace = Path.cwd() / "codibase-workspace"
    workspace_input = input(f"\nWorkspace directory [{default_workspace}]: ").strip()
    workspace_dir = Path(workspace_input) if workspace_input else default_workspace
    
    # Create workspace directory
    workspace_dir.mkdir(parents=True, exist_ok=True)
    log_success(f"Workspace directory: {workspace_dir}\n")
    
    # Clone each repository
    success_count = 0
    for repo_key, config in REPOS.items():
        if clone_repo_clean(repo_key, config, workspace_dir):
            success_count += 1
    
    # Create workspace README
    create_workspace_readme(workspace_dir)
    
    # Summary
    log(f"\n{Colors.BOLD}{'='*60}{Colors.RESET}")
    log(f"{Colors.BOLD}{Colors.GREEN}SUMMARY{Colors.RESET}")
    log(f"{Colors.BOLD}{'='*60}{Colors.RESET}")
    log(f"Successfully cloned: {success_count}/{len(REPOS)} repositories")
    log(f"Workspace location: {workspace_dir}")
    
    if success_count == len(REPOS):
        log_success("\n✓ All repositories cloned successfully!")
        log(f"\n{Colors.YELLOW}Next steps:{Colors.RESET}")
        log(f"  1. cd {workspace_dir}")
        log(f"  2. git init")
        log(f"  3. git add .")
        log(f"  4. git commit -m 'Initial commit: CodiBase stack'")
        log(f"  5. Start building! 🚀\n")
    else:
        log_warning("\n⚠ Some repositories failed to clone. Check errors above.")
        sys.exit(1)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        log_error("\n\nScript interrupted by user")
        sys.exit(1)
    except Exception as e:
        log_error(f"\n\nUnexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
