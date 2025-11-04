import { describe, expect, it } from 'vitest'
import { findClosestGitRepository } from '../lib/utils/repository'
import type { GitRepository } from 'pilotcodi-chat-panel'

describe('findClosestGitRepository', () => {
  it('should match .git suffix', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/example/test' },
    ]
    const result = findClosestGitRepository(repositories, 'https://github.com/example/test.git')
    expect(result).toEqual(repositories[0])
  })

  it('should match auth in URL', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/example/test' },
    ]
    const result = findClosestGitRepository(repositories, 'https://creds@github.com/example/test')
    expect(result).toEqual(repositories[0])
  })

  it('should not match different names', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/example/anoth-repo' },
    ]
    const result = findClosestGitRepository(repositories, 'https://github.com/example/another-repo')
    expect(result).toBeUndefined()
  })

  it('should not match repositories with a common prefix', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/PilotCodiML/registry-pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'https://github.com/PilotCodiML/pilotcodi')
    expect(result).toBeUndefined()
  })

  it('should not match entirely different repository names', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/PilotCodiML/uptime' },
    ]
    const result = findClosestGitRepository(repositories, 'https://github.com/PilotCodiML/pilotcodi')
    expect(result).toBeUndefined()
  })

  it('should not match URL without repository name', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/PilotCodiML/pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'https://github.com')
    expect(result).toBeUndefined()
  })

  it('should match different host', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/PilotCodiML/pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'https://bitbucket.com/PilotCodiML/pilotcodi')
    expect(result).toEqual(repositories[0])
  })

  it('should not match multiple close matches', () => {
    const repositories: GitRepository[] = [
      { url: 'https://bitbucket.com/CrabbyML/crabby' },
      { url: 'https://gitlab.com/PilotCodiML/registry-pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'git@github.com:PilotCodiML/pilotcodi')
    expect(result).toBeUndefined()
  })

  it('should match different protocol and suffix', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/PilotCodiML/pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'git@github.com:PilotCodiML/pilotcodi.git')
    expect(result).toEqual(repositories[0])
  })

  it('should match different protocol', () => {
    const repositories: GitRepository[] = [
      { url: 'https://github.com/PilotCodiML/pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'git@github.com:PilotCodiML/pilotcodi')
    expect(result).toEqual(repositories[0])
  })

  it('should match URL without organization', () => {
    const repositories: GitRepository[] = [
      { url: 'https://custom-git.com/PilotCodiML/pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'https://custom-git.com/pilotcodi')
    expect(result).toEqual(repositories[0])
  })

  it('should match local URL', () => {
    const repositories: GitRepository[] = [
      { url: 'file:///home/PilotCodiML/pilotcodi' },
    ]
    const result = findClosestGitRepository(repositories, 'git@github.com:PilotCodiML/pilotcodi.git')
    expect(result).toEqual(repositories[0])
  })
})
