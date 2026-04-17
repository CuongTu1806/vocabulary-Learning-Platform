# Module 4: Space Repetition

## 1) Muc tieu
Module 4 giup nguoi dung on tap tu vung theo co che lap lai ngat quang (Spaced Repetition), ket hop:
- Learning steps cho the tu moi hoac vua hoc.
- Review dai han theo thuat toan SM-2 da tuy chinh.
- Relearning khi do kho cao hoac quen nhieu.

## 2) Du lieu cot loi (Card)
Moi flashcard can cac truong chinh:
- `ivl` (interval): khoang cach giua 2 lan on tap (thong thuong theo ngay trong review; co the la phut trong learning).
- `due`: thoi diem den han hien the tiep theo.
- `easeFactor` (EF): he so de nho, mac dinh `2.5`.
- `delayFactor`: he so phat khi tre han (tre cang lau, EF giam cang nhieu).

Gia tri khoi tao de xuat:
- `interval = 1`
- `easeFactor = 2.5`

## 3) Trang thai hoc tap
- `new`: the moi, chua hoc lan nao.
- `learning`: dang di qua chuoi step ngan han (1m, 10m, 30m, 1h).
- `review`: da qua learning, on tap dai han bang cong thuc SM-2.
- `relearning`: quay lai hoc lai khi kha nang nho giam (thuong khi EF qua thap hoac bam Again).

## 4) Luong Learning (4 steps)
De xuat 4 moc:
- Step 1: 1 phut
- Step 2: 10 phut
- Step 3: 30 phut
- Step 4: 1 gio

Ket qua tra loi o learning:
- `Forgot`: quay lai Step 1.
- `Partially recalled`: giu nguyen step, lap lai voi thoi gian bang 1/2 step hien tai.
- `Recalled with effort`: len step tiep theo.
- `Easily recalled`:
  - Neu step < 3: len step tiep theo.
  - Neu step >= 3: cho phep vao review som.

Khi hoan tat Step 4 thi chuyen sang `review`.

## 5) Luong Review (SM-2 tuy chinh)
Cac muc danh gia:
- `again` (q = 0)
- `hard` (q = 3)
- `good` (q = 4)
- `easy` (q = 5)

### 5.1 Cong thuc Ease Factor
- Rang buoc: `EF >= 1.5`
- Cong thuc:

```text
EF_new = EF_old + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
```

Neu tre han (lam bai sau due), EF co the bi giam them theo `delayFactor`.

### 5.2 Cong thuc Interval
Gia su `I_old` la interval hien tai:
- `again`: `I_new = I_old / 10`
- `hard`: `I_new = I_old * 1.2`
- `good`: `I_new = I_old * EF`
- `easy`: `I_new = I_old * EF * 1.3` (easy bonus)

Rang buoc:
- Neu `I_new > maxInterval` thi gan bang `maxInterval`.
- Sau moi lan tra loi, cap nhat `due` dua tren `I_new`.

## 6) Rule chuyen trang thai
- `new` -> `learning`: khi bat dau hoc the.
- `learning` -> `review`: hoan tat day du cac step ma khong bi reset ve dau.
- `review` -> `relearning`: khi ket qua yeu (thuong do `again` hoac EF xuong qua nguong).
- `relearning` -> `review`: hoan tat chu ky hoc lai.

## 7) Nhac lich va thong bao
- He thong tao lich den han dua tren `due`.
- Moi ngay (0h) tong hop:
  - So the can hoc (`learning`).
  - So the can on (`review`).
- Neu den han nhung nguoi dung chua lam ngay, trang thai van duoc giu de nhac tiep o lan sau.

## 8) Hanh vi UI de xuat
- Man hinh Space Repetition hien lich 1 thang, moi ngay co so luong the den han.
- Vao phien on tap:
  - Hien tung flashcard.
  - Nguoi dung tu nho nghia/tu.
  - Chon muc danh gia (`again`, `hard`, `good`, `easy`) de cap nhat EF + interval + due.

## 9) Pseudo-flow backend
```text
1. Lay danh sach card den han (due <= now).
2. Xac dinh mode card: learning/review/relearning.
3. Nhan ket qua danh gia tu nguoi dung.
4. Tinh EF moi, interval moi theo rule.
5. Cap nhat due, status, lich su on tap.
6. Tra ve card tiep theo.
```

## 10) Ghi chu implementation
- Nen luu lich su tung lan tra loi (quality, EF cu/moi, interval cu/moi, timestamp) de:
  - audit
  - thong ke tien trinh hoc
  - tinh toan nang cao ve sau
- Nen cau hinh duoc:
  - bo learning steps
  - max interval
  - easy bonus
  - delayFactor
