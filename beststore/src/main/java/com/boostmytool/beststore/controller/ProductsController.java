package com.boostmytool.beststore.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.boostmytool.beststore.BeststoreApplication;
import com.boostmytool.beststore.model.Product;
import com.boostmytool.beststore.model.ProductDto;
import com.boostmytool.beststore.repository.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

	private final BeststoreApplication beststoreApplication;

	@Autowired
	private ProductsRepository repo;

	ProductsController(BeststoreApplication beststoreApplication) {
		this.beststoreApplication = beststoreApplication;
	}

	@GetMapping({ "", "/" })
	public String showProductList(Model model) {
		List<Product> products = repo.findAll();
		for (Product product : products) {
			System.out.println("Image file for product " + product.getName() + ": " + product.getImageFileName());
		}
		model.addAttribute("products", products);
		return "products/index";
	}

	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/CreateProduct";
	}

	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {

		if (productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}

		if (result.hasErrors()) {
			return "products/CreateProduct";
		}
		MultipartFile image = productDto.getImageFile();
		Date createdAt = new Date();
		String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

		try {
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);

			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath, null);
			}

			try (InputStream inputStream = image.getInputStream()) {
				Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreateDate(createdAt);
		product.setImageFileName(storageFileName);

		repo.save(product);

		return "redirect:/products";
	}

	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam int id) {

		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);

			ProductDto productDto = new ProductDto();
			productDto.setName(product.getName());
			productDto.setBrand(product.getBrand());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDescription());

			model.addAttribute("productDto", productDto);

		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			return "redirect:/products";
		}
		return "products/EditProduct";
	}

	@PostMapping("/edit")
	public String updateProducts(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto,
			BindingResult result) {

		try {
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);

			if (result.hasErrors()) {
				return "products/EditProduct";
			}
			if (!productDto.getImageFile().isEmpty()) {
				// delete old image
				String uploadDir = "public/images/";
				Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

				try {
					Files.delete(oldImagePath);
				} catch (Exception e) {
					System.out.println("Exception:" + e.getMessage());
				}

				// save new image file
				MultipartFile image = productDto.getImageFile();
				Date createdAt = new Date();
				String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

				try (InputStream inputStream = image.getInputStream()) {
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
							StandardCopyOption.REPLACE_EXISTING);
				}

				product.setImageFileName(storageFileName);
			}

			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());

			repo.save(product);

		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}

		return "redirect:/products";

	}

	@GetMapping("/delete")
	public String deleteProduct(@RequestParam int id) {

		try {
			Product product = repo.findById(id).get();
			
			// delete product image
			Path imagePath = Paths.get("public/image/" + product.getImageFileName());
			
			try {
				Files.delete(imagePath);
			}
			catch(Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
			
			//delete the product
			repo.delete(product);
			
			
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());

		}
		return "redirect:/products";
	}

}
