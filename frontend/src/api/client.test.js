import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from './client'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('api', () => {
  it('devolve o corpo quando a resposta é bem-sucedida', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      json: async () => [{ id: 1 }],
    }))

    await expect(api('/produtos')).resolves.toEqual([{ id: 1 }])
    expect(fetch).toHaveBeenCalledWith('/api/produtos', expect.objectContaining({
      headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
    }))
  })

  it('devolve nulo quando não há conteúdo', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 204,
      ok: true,
      json: async () => ({}),
    }))

    await expect(api('/produtos/1', { method: 'DELETE' })).resolves.toBeNull()
  })

  it('expõe a mensagem e os campos do erro da API', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 422,
      ok: false,
      json: async () => ({ mensagem: 'Saldo insuficiente', codigo: 'SALDO', campos: { quantidade: 'alta' } }),
    }))

    await expect(api('/movimentos')).rejects.toMatchObject({
      message: 'Saldo insuficiente',
      codigo: 'SALDO',
      campos: { quantidade: 'alta' },
      status: 422,
    })
  })

  it('usa uma mensagem padrão quando a API não explica o erro', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 500,
      ok: false,
      json: async () => {
        throw new Error('sem json')
      },
    }))

    await expect(api('/produtos')).rejects.toMatchObject({
      message: 'Não foi possível concluir a operação.',
      campos: {},
    })
  })

  it('avisa quando não consegue conectar', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('offline')))

    await expect(api('/produtos')).rejects.toMatchObject({
      message: 'Não foi possível conectar à API. Verifique se o backend está em execução.',
      campos: {},
    })
  })
})
