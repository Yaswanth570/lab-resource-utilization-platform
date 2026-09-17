import { apiClient } from './client';
import type {
  BookingResponse,
  CreateBookingRequest,
  ConfirmBookingRequest,
  CancelBookingRequest,
  BookingFilterParams,
} from '../types/booking';

// List bookings with optional server-side filtering
export const getBookings = async (
  params?: BookingFilterParams
): Promise<BookingResponse[]> => {
  const response = await apiClient.get<BookingResponse[]>('/bookings', {
    params,
  });
  return response.data;
};

// Retrieve a single booking by ID
export const getBookingById = async (
  id: number | string
): Promise<BookingResponse> => {
  const response = await apiClient.get<BookingResponse>(`/bookings/${id}`);
  return response.data;
};

// Retrieve a single booking by booking reference
export const getBookingByReference = async (
  reference: string
): Promise<BookingResponse> => {
  const response = await apiClient.get<BookingResponse>(
    `/bookings/reference/${reference}`
  );
  return response.data;
};

// Create a new booking
export const createBooking = async (
  data: CreateBookingRequest
): Promise<BookingResponse> => {
  const response = await apiClient.post<BookingResponse>('/bookings', data);
  return response.data;
};

// Confirm a pending booking (approver optional, defaults to authenticated user on backend)
export const confirmBooking = async (
  id: number | string,
  data?: ConfirmBookingRequest
): Promise<BookingResponse> => {
  const response = await apiClient.patch<BookingResponse>(
    `/bookings/${id}/confirm`,
    data
  );
  return response.data;
};

// Cancel an active booking (canceller optional, cancellationReason optional)
export const cancelBooking = async (
  id: number | string,
  data?: CancelBookingRequest
): Promise<BookingResponse> => {
  const response = await apiClient.patch<BookingResponse>(
    `/bookings/${id}/cancel`,
    data
  );
  return response.data;
};

// Transition booking to IN_USE
export const startBooking = async (
  id: number | string
): Promise<BookingResponse> => {
  const response = await apiClient.patch<BookingResponse>(
    `/bookings/${id}/start`
  );
  return response.data;
};

// Transition booking to COMPLETED
export const completeBooking = async (
  id: number | string
): Promise<BookingResponse> => {
  const response = await apiClient.patch<BookingResponse>(
    `/bookings/${id}/complete`
  );
  return response.data;
};

// Mark booking as NO_SHOW
export const markNoShow = async (
  id: number | string
): Promise<BookingResponse> => {
  const response = await apiClient.patch<BookingResponse>(
    `/bookings/${id}/no-show`
  );
  return response.data;
};
