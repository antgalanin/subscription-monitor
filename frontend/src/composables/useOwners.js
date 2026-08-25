import { computed, ref } from 'vue'
import { usersApi } from '../api'
import { useAuthStore } from '../stores/auth'

const owners = ref([])
const loaded = ref(false)

export function invalidateOwners() {
  loaded.value = false
}

export function useOwners() {
  const auth = useAuthStore()
  const showOwner = computed(() => auth.isAdmin)
  const byId = computed(() => new Map(owners.value.map((user) => [user.id, user.username])))

  async function loadOwners() {
    if (!auth.isAdmin || loaded.value) return
    try {
      const { data } = await usersApi.list()
      owners.value = data
      loaded.value = true
    } catch {
      owners.value = []
    }
  }

  function ownerName(userId) {
    if (!userId) return '—'
    if (userId === auth.user?.id) return `${auth.user.username} (вы)`
    return byId.value.get(userId) ?? '—'
  }

  function isForeign(userId) {
    return Boolean(userId) && userId !== auth.user?.id
  }

  return { owners, showOwner, loadOwners, ownerName, isForeign }
}
