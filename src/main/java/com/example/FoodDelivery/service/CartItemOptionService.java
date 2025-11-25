package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.CartItemOption;
import com.example.FoodDelivery.domain.CartItem;
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.CartItemOptionRepository;
import com.example.FoodDelivery.repository.CartItemRepository;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class CartItemOptionService {
    private final CartItemOptionRepository cartItemOptionRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuOptionRepository menuOptionRepository;

    public CartItemOptionService(CartItemOptionRepository cartItemOptionRepository,
            CartItemRepository cartItemRepository,
            MenuOptionRepository menuOptionRepository) {
        this.cartItemOptionRepository = cartItemOptionRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuOptionRepository = menuOptionRepository;
    }

    public CartItemOption getCartItemOptionById(Long id) {
        Optional<CartItemOption> cartItemOptionOpt = this.cartItemOptionRepository.findById(id);
        return cartItemOptionOpt.orElse(null);
    }

    public List<CartItemOption> getCartItemOptionsByCartItemId(Long cartItemId) {
        return this.cartItemOptionRepository.findByCartItemId(cartItemId);
    }

    public CartItemOption createCartItemOption(CartItemOption cartItemOption) throws IdInvalidException {
        // check cart item exists
        if (cartItemOption.getCartItem() != null) {
            CartItem cartItem = this.cartItemRepository.findById(cartItemOption.getCartItem().getId()).orElse(null);
            if (cartItem == null) {
                throw new IdInvalidException("Cart item not found with id: " + cartItemOption.getCartItem().getId());
            }
            cartItemOption.setCartItem(cartItem);
        } else {
            throw new IdInvalidException("Cart item is required");
        }

        // check menu option exists
        if (cartItemOption.getOption() != null) {
            MenuOption option = this.menuOptionRepository.findById(cartItemOption.getOption().getOptionId())
                    .orElse(null);
            if (option == null) {
                throw new IdInvalidException(
                        "Menu option not found with id: " + cartItemOption.getOption().getOptionId());
            }
            cartItemOption.setOption(option);
        } else {
            throw new IdInvalidException("Menu option is required");
        }

        return cartItemOptionRepository.save(cartItemOption);
    }

    public CartItemOption updateCartItemOption(CartItemOption cartItemOption) throws IdInvalidException {
        // check id
        CartItemOption currentCartItemOption = getCartItemOptionById(cartItemOption.getId());
        if (currentCartItemOption == null) {
            throw new IdInvalidException("Cart item option not found with id: " + cartItemOption.getId());
        }

        if (cartItemOption.getCartItem() != null) {
            CartItem cartItem = this.cartItemRepository.findById(cartItemOption.getCartItem().getId()).orElse(null);
            if (cartItem == null) {
                throw new IdInvalidException("Cart item not found with id: " + cartItemOption.getCartItem().getId());
            }
            currentCartItemOption.setCartItem(cartItem);
        }

        if (cartItemOption.getOption() != null) {
            MenuOption option = this.menuOptionRepository.findById(cartItemOption.getOption().getOptionId())
                    .orElse(null);
            if (option == null) {
                throw new IdInvalidException(
                        "Menu option not found with id: " + cartItemOption.getOption().getOptionId());
            }
            currentCartItemOption.setOption(option);
        }

        return cartItemOptionRepository.save(currentCartItemOption);
    }

    public ResultPaginationDTO getAllCartItemOptions(Specification<CartItemOption> spec, Pageable pageable) {
        Page<CartItemOption> page = this.cartItemOptionRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public void deleteCartItemOption(Long id) {
        this.cartItemOptionRepository.deleteById(id);
    }

    @Transactional
    public void deleteCartItemOptionsByCartItemId(Long cartItemId) {
        this.cartItemOptionRepository.deleteByCartItemId(cartItemId);
    }
}
