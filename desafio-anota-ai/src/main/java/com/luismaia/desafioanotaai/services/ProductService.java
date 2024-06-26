package com.luismaia.desafioanotaai.services;

import com.luismaia.desafioanotaai.domain.category.Category;
import com.luismaia.desafioanotaai.domain.category.exceptions.CategoryNotFoundException;
import com.luismaia.desafioanotaai.domain.product.Product;
import com.luismaia.desafioanotaai.domain.product.ProductDTO;
import com.luismaia.desafioanotaai.domain.product.exceptions.ProductNotFoundException;
import com.luismaia.desafioanotaai.repositories.ProductRepository;
import com.luismaia.desafioanotaai.services.aws.AwsSnsService;
import com.luismaia.desafioanotaai.services.aws.MessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductService {
    private final ProductRepository repository;
    private final CategoryService categoryService;
    private final AwsSnsService awsSnsService;
    public ProductService(ProductRepository repository, CategoryService categoryService, AwsSnsService awsSnsService){
        this.repository = repository;
        this.categoryService = categoryService;
        this.awsSnsService = awsSnsService;
    }

    public Product insert(ProductDTO productData){
        Category category = this.categoryService.getById(productData.categoryId()).orElseThrow(CategoryNotFoundException::new);
        Product newProduct = new Product(productData);
        newProduct.setCategory(category);
        this.repository.save(newProduct);

        this.awsSnsService.publish(new MessageDTO(newProduct.getOwnerId()));
        return newProduct;
    }

    public Product update(String id, ProductDTO productData){
        Product product = this.repository.findById(id).orElseThrow(ProductNotFoundException::new);
        if(productData.categoryId() != null) {
            this.categoryService.getById(productData.categoryId()).ifPresent(product::setCategory);
        }
        if(!productData.title().isEmpty()) product.setTitle(productData.title());
        if(!productData.description().isEmpty()) product.setDescription(productData.description());
        if(!(productData.price() == null)) product.setPrice(productData.price());

        this.repository.save(product);
        this.awsSnsService.publish(new MessageDTO(product.getOwnerId()));
        return product;
    }

    public void delete(String id){
        Product product = this.repository.findById(id).orElseThrow(ProductNotFoundException::new);
        this.repository.delete(product);
    }

    public List<Product> getAll(){
        return this.repository.findAll();
    }
}
