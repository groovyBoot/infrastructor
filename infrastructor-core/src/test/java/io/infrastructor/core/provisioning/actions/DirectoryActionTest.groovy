package io.infrastructor.core.provisioning.actions

import io.infrastructor.core.inventory.InventoryAwareTestBase
import io.infrastructor.core.provisioning.TaskExecutionException
import org.junit.Test

class DirectoryActionTest extends InventoryAwareTestBase {

    @Test
    void "create a directory as root"() {
        def result
        withUser(DEVOPS) {
            it.provision {
                task actions: {
                    user user: 'root', name: "testuser"
                    group user: 'root', name: "testgroup"
                    directory user: 'root', target: '/var/simple', owner: 'testuser', group: 'testgroup', mode: '0600'
                    result = shell user: 'root', command: "ls -dalh /var/simple"
                }
            }
        }

        assert result.output.contains("simple")
        assert result.output.contains("testuser testgroup")
        assert result.output.contains("drw------")
    }


    @Test
    void "create a directory as devops with sudo"() {
        def result
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory user: 'root', target: '/etc/simple', owner: DEVOPS, group: DEVOPS, mode: '0600'
                    result = shell("ls -dalh /etc/simple")
                }
            }
        }

        assert result.output.contains("simple")
        assert result.output.contains("devops devops")
        assert result.output.contains("drw------")
    }

    @Test
    void "create a directory as sudops with sudo and a password"() {
        def result
        withUser(SUDOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory user: 'root', sudopass: SUDOPS, target: '/etc/simple', owner: SUDOPS, group: SUDOPS, mode: '600'
                    result = shell("ls -dalh /etc/simple")
                }
            }
        }

        assert result.output.contains("simple")
        assert result.output.contains("sudops sudops")
        assert result.output.contains("drw------")
    }

    @Test(expected = TaskExecutionException)
    void "create a directory as sudops with sudo and a wrong password"() {
        withUser(SUDOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory user: 'root', sudopass: 'wrong', target: '/etc/simple', owner: SUDOPS, group: SUDOPS, mode: '600'
                }
            }
        }
    }

    @Test
    void "create a directory as devops without any password"() {
        def result
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory user: 'root', sudopass: DEVOPS, target: '/etc/simple', owner: SUDOPS, group: SUDOPS, mode: '600'
                    result = shell("ls -dalh /etc/simple")
                }
            }
        }

        assert result.output.contains("simple")
        assert result.output.contains("sudops sudops")
        assert result.output.contains("drw------")
    }

    @Test
    void "create nested directories"() {
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    // execute
                    directory user: 'root', target: '/etc/simple/deep', mode: '600'

                    def resultDeep = shell user: 'root', command: 'ls -ldah /etc/simple/deep'
                    // assert
                    assert resultDeep.exitcode == 0
                    assert resultDeep.output.contains('deep')
                    assert resultDeep.output.contains("root root")
                    assert resultDeep.output.contains("drw-------")

                    def resultSimple = shell user: 'root', command: 'ls -ldah /etc/simple'
                    // assert
                    assert resultSimple.exitcode == 0
                    assert resultSimple.output.contains('simple')
                    assert resultSimple.output.contains("root root")
                    assert resultSimple.output.contains("drwxr-xr-x")

                    def resultEtc = shell user: 'root', command: 'ls -ldah /etc'
                    // assert
                    assert resultEtc.exitcode == 0
                    assert resultEtc.output.contains('etc')
                    assert resultEtc.output.contains("root root")
                    assert resultEtc.output.contains("drwxr-xr-x")
                }
            }
        }
    }

    @Test(expected = TaskExecutionException)
    void "create a directory as devops without sudo"() {
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory target: '/etc/simple'
                }
            }
        }
    }

    @Test(expected = TaskExecutionException)
    void "create a directory with an unknown owner"() {
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory target: '/etc/simple', owner: 'doesnotexist'
                }
            }
        }
    }

    @Test(expected = TaskExecutionException)
    void "create a directory with an unknown group"() {
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory target: '/etc/simple', group: 'doesnotexist'
                }
            }
        }
    }

    @Test(expected = TaskExecutionException)
    void "create a directory with an invalid mode"() {
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    directory user: 'root', target: '/etc/simple', mode: '8888'
                }
            }
        }
    }

    @Test
    void createDirectoryWithEmptyMode() {
        withUser(DEVOPS) { inventory ->
            inventory.provision {
                task actions: {
                    assert directory(user: 'root', target: '/etc/simple/test1', mode: '').exitcode == 0
                    assert shell("ls -alhd /etc/simple/test1").output.contains("drwxr-xr-x")

                    assert directory(user: 'root', target: '/etc/simple/test2', mode: null).exitcode == 0
                    assert shell("ls -alhd /etc/simple/test2").output.contains("drwxr-xr-x")

                    assert directory(user: 'root', target: '/etc/simple/test3', mode: 0).exitcode == 0
                    assert shell("ls -alhd /etc/simple/test3").output.contains("drwxr-xr-x")
                }
            }
        }
    }
}

