package com.nexdom.estoque.support;

import com.nexdom.estoque.domain.model.Produto;

public final class ProdutoFixture {

    private ProdutoFixture() {
    }

    public static Produto comId(Produto produto, Long id) {
        try {
            var idField = Produto.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(produto, id);
            return produto;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
