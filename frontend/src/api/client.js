export async function api(path, options = {}) {
  let response
  try {
    response = await fetch(`/api${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
    })
  } catch {
    throw Object.assign(new Error('Não foi possível conectar à API. Verifique se o backend está em execução.'), {
      campos: {},
    })
  }

  if (response.status === 204) {
    return null
  }

  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    const error = new Error(data.mensagem || 'Não foi possível concluir a operação.')
    error.codigo = data.codigo
    error.campos = data.campos || {}
    error.status = response.status
    throw error
  }
  return data
}
